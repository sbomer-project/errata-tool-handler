#!/bin/bash
# Triggers a UMB event in the local Artemis instance.
# Usage: ./trigger-umb.sh [ERRATA_ID] [STATUS]

ERRATA_ID=${1:-123456}
STATUS=${2:-QE}
BROKER_NAME="broker"
USER="admin"
PASSWORD="admin"
BASE_URL="http://localhost:8161/console/jolokia"

echo "🔍 Checking available addresses on broker '${BROKER_NAME}'..."

# 1. Fetch address names to find the right one
ADDRESS_LIST=$(curl -s -u $USER:$PASSWORD -H "Origin: http://localhost:8161" \
    "${BASE_URL}/read/org.apache.activemq.artemis:broker=\"${BROKER_NAME}\"/AddressNames")

if echo "$ADDRESS_LIST" | grep -q "\"errata.activity.status\""; then
    TARGET_ADDR="errata.activity.status"
elif echo "$ADDRESS_LIST" | grep -q "\"errata\""; then
    TARGET_ADDR="errata"
else
    echo "❌ Could not find an address named 'errata' or 'errata.activity.status'."
    echo "   Available: $ADDRESS_LIST"
    exit 1
fi

echo "🚀 Triggering event for Errata ID: $ERRATA_ID ($STATUS) to address '$TARGET_ADDR'..."

# 2. Prepare Data
MBEAN="org.apache.activemq.artemis:broker=\\\"${BROKER_NAME}\\\",component=addresses,address=\\\"${TARGET_ADDR}\\\""
HEADERS="{\"subject\":\"errata.activity.status\"}"
BODY="{\"errata_id\": $ERRATA_ID, \"errata_status\": \"$STATUS\"}"

# --- ENCODING ---
if command -v openssl &> /dev/null; then
    BODY_ENCODED=$(echo -n "$BODY" | openssl base64 | tr -d '\n')
else
    BODY_ENCODED=$(echo -n "$BODY" | base64 | tr -d '\n')
fi

# 3. Construct Payload
REQ_JSON=$(cat <<EOF
{
    "type": "EXEC",
    "mbean": "$MBEAN",
    "operation": "sendMessage(java.util.Map,int,java.lang.String,boolean,java.lang.String,java.lang.String)",
    "arguments": [$HEADERS, 4, "$BODY_ENCODED", false, "$USER", "$PASSWORD"]
}
EOF
)

# 4. Execute
# We verify the response value to ensure success
RESPONSE=$(curl -s -u $USER:$PASSWORD \
     -H "Origin: http://localhost:8161" \
     -H "Content-Type: application/json" \
     -d "$REQ_JSON" \
     "$BASE_URL")

if echo "$RESPONSE" | grep -q "\"status\":200"; then
    echo "✅ Event sent successfully!"
else
    echo "❌ Failed to send event."
    echo "Response: $RESPONSE"
fi