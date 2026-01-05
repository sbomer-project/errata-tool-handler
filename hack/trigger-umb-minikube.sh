#!/bin/bash
# Triggers a UMB event by compiling and running a custom Java Sender inside the pod.
# Fixes "unexpected EOF" errors by using pipe streaming instead of variables.

ERRATA_ID=${1:-123456}
STATUS=${2:-QE}
NAMESPACE="sbomer-test"
LABEL_SELECTOR="app=amqp-broker"
USER="admin"
PASSWORD="password"

# CONFIGURATION
# We use Single Prefix because the Java client takes the name literally.
TOPIC_NAME="topic://errata.events"
SUBJECT="errata.activity.status"

echo "🔍 Finding AMQ Pod..."
POD=$(kubectl get pod -n $NAMESPACE -l $LABEL_SELECTOR -o jsonpath="{.items[0].metadata.name}")

if [ -z "$POD" ]; then
    echo "❌ No pod found."
    exit 1
fi

# Prepare the JSON Body
MESSAGE_BODY="{\"errata_id\": $ERRATA_ID, \"errata_status\": \"$STATUS\"}"
# Escape quotes in the body so they are valid inside the Java string source
JAVA_SAFE_BODY=$(echo $MESSAGE_BODY | sed 's/"/\\"/g')

echo "🚀 Streaming Java Sender to pod..."

# 1. Stream the Java file directly to the pod
# We use 'tee' inside the pod to read from stdin and write to the file.
# We check for both 'jakarta.jms' and 'javax.jms' compatibility.
cat <<EOF | kubectl exec -i -n $NAMESPACE $POD -- tee /tmp/UmbSender.java > /dev/null
import javax.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class UmbSender {
    public static void main(String[] args) throws Exception {
        String topicName = "$TOPIC_NAME";
        String subject   = "$SUBJECT";
        String jsonBody  = "$JAVA_SAFE_BODY";
        
        System.out.println("Connecting to broker...");
        // Use the internal container port
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616", "$USER", "$PASSWORD");
        
        Connection connection = null;
        Session session = null;
        
        try {
            connection = cf.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             
            // Create Topic
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            
            // CRITICAL: Create BytesMessage for your App
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(jsonBody.getBytes("UTF-8"));
            
            // Set the Subject Header
            message.setStringProperty("subject", subject);
            
            producer.send(message);
            
            System.out.println("✅ SENT BytesMessage to: " + topicName);
            System.out.println("   Subject: " + subject);
            System.out.println("   Body length: " + jsonBody.length());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }
}
EOF

echo "🚀 Compiling and Running Sender..."

# 2. Run it using the 'java' binary inside the pod.
# We add all potential lib paths to the classpath.
kubectl exec -n $NAMESPACE $POD -- bash -c "export CLASSPATH=\"/opt/amq/lib/*:/opt/amq/lib/client/*:.\"; java /tmp/UmbSender.java"

if [ $? -eq 0 ]; then
    echo "🎉 Done!"
else
    echo "❌ Failed to run Java sender."
    echo "   (If you get a 'package does not exist' error, the image might use 'jakarta.jms' instead of 'javax.jms'. Let me know!)"
    exit 1
fi