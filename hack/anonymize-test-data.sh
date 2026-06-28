#!/bin/bash

# Script to anonymize sensitive data in errata-tool test JSON files
# Usage: ./hack/anonymize-test-data.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TEST_DATA_DIR="$PROJECT_ROOT/src/test/resources/errata-responses"

echo "Anonymizing test data in: $TEST_DATA_DIR"

# Function to anonymize builds.json files
anonymize_builds_json() {
    local file="$1"
    echo "  Processing builds file: $file"
    
    # Create a temporary file
    local temp_file="${file}.tmp"
    
    # Use jq to anonymize sig_key and container_sig_key
    jq '
        walk(
            if type == "object" then
                if has("sig_key") then
                    .sig_key = {
                        "name": "anonymized-key",
                        "keyid": "00000000"
                    }
                else . end |
                if has("container_sig_key") then
                    .container_sig_key = {
                        "name": "anonymized-key",
                        "keyid": "00000000"
                    }
                else . end |
                if has("added_by") then
                    .added_by = "anonymized-user@example.com"
                else . end
            else . end
        )
    ' "$file" > "$temp_file"
    
    # Replace original file
    mv "$temp_file" "$file"
}

# Function to anonymize errata.json files
anonymize_errata_json() {
    local file="$1"
    echo "  Processing errata file: $file"
    
    # Create a temporary file
    local temp_file="${file}.tmp"
    
    # Use jq to anonymize user data, IDs, and CVEs
    jq '
        # Function to anonymize CVE references
        def anonymize_cve:
            gsub("CVE-[0-9]{4}-[0-9]+"; "CVE-XXXX-XXXXX");
        
        # Walk through the entire structure
        walk(
            if type == "object" then
                # Anonymize user object
                if has("user") then
                    .user = {
                        "id": 9999999,
                        "login_name": "anonymized-user@example.com",
                        "realname": "Anonymized User",
                        "user_organization_id": 999,
                        "enabled": .user.enabled,
                        "receives_mail": .user.receives_mail,
                        "preferences": {},
                        "email_address": "anonymized-user@example.com",
                        "account_name": "anonymized-user",
                        "type": .user.type
                    }
                else . end |
                
                # Anonymize kerberos_principal_owner
                if has("kerberos_principal_owner") then
                    .kerberos_principal_owner = {
                        "name": "anonymized-service@example.com",
                        "description": "Anonymized service account"
                    }
                else . end |
                
                # Anonymize reporter_id, assigned_to_id, package_owner_id, manager_id
                if has("reporter_id") then
                    .reporter_id = 9999991
                else . end |
                if has("assigned_to_id") then
                    .assigned_to_id = 9999992
                else . end |
                if has("package_owner_id") then
                    .package_owner_id = 9999993
                else . end |
                if has("manager_id") then
                    .manager_id = 9999994
                else . end |
                if has("doc_reviewer_id") then
                    .doc_reviewer_id = 9999995
                else . end |
                if has("product_security_reviewer_id") and .product_security_reviewer_id != null then
                    .product_security_reviewer_id = 9999996
                else . end
            else . end |
            
            # Anonymize CVE references in strings
            if type == "string" then
                anonymize_cve
            else . end
        )
    ' "$file" > "$temp_file"
    
    # Replace original file
    mv "$temp_file" "$file"
}

# Process all builds.json files
echo ""
echo "=== Anonymizing builds.json files ==="
find "$TEST_DATA_DIR" -type f -name "*_builds.json" | while read -r file; do
    anonymize_builds_json "$file"
done

# Process all errata.json files
echo ""
echo "=== Anonymizing errata.json files ==="
find "$TEST_DATA_DIR" -type f -name "*_errata.json" | while read -r file; do
    anonymize_errata_json "$file"
done

echo ""
echo "=== Anonymization complete ==="
echo ""
echo "Summary of changes:"
echo "  - Replaced sig_key and container_sig_key with placeholder values"
echo "  - Replaced added_by with anonymized-user@example.com"
echo "  - Anonymized all user objects (IDs, names, emails, org IDs)"
echo "  - Anonymized reporter_id, assigned_to_id, package_owner_id, manager_id"
echo "  - Replaced CVE references with CVE-XXXX-XXXXX"
echo ""

# Made with Bob
