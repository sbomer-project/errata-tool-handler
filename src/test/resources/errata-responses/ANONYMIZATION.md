# Test Data Anonymization

This directory contains anonymized test data from the Errata Tool API for testing purposes.

## Anonymization Script

The test data has been anonymized using the script: `hack/anonymize-test-data.sh`

### What Gets Anonymized

#### In `*_builds.json` files:
- **sig_key**: Replaced with placeholder values
  - `name`: "anonymized-key"
  - `keyid`: "00000000"
- **container_sig_key**: Replaced with placeholder values
  - `name`: "anonymized-key"
  - `keyid`: "00000000"
- **added_by**: Replaced with "anonymized-user@example.com"

#### In `*_errata.json` files:
- **User objects** (`who.user`):
  - `id`: 9999999
  - `login_name`: "anonymized-user@example.com"
  - `realname`: "Anonymized User"
  - `user_organization_id`: 999
  - `email_address`: "anonymized-user@example.com"
  - `account_name`: "anonymized-user"
  - Preserves: `enabled`, `receives_mail`, `type`

- **Kerberos Principal Owner**:
  - `name`: "anonymized-service@example.com"
  - `description`: "Anonymized service account"

- **ID Fields**:
  - `reporter_id`: 9999991
  - `assigned_to_id`: 9999992
  - `package_owner_id`: 9999993
  - `manager_id`: 9999994
  - `doc_reviewer_id`: 9999995
  - `product_security_reviewer_id`: 9999996

- **CVE References**: All CVE references (e.g., CVE-2025-9230) are replaced with "CVE-XXXX-XXXXX"

### Running the Anonymization Script

From the project root:

```bash
./hack/anonymize-test-data.sh
```

The script will:
1. Find all `*_builds.json` files and anonymize them
2. Find all `*_errata.json` files and anonymize them
3. Replace files in-place (creates temporary files during processing)

### Requirements

- `jq` - Command-line JSON processor
- `bash` - Shell script interpreter

Install jq:
```bash
# On macOS
brew install jq

# On RHEL/Fedora
sudo dnf install jq

# On Ubuntu/Debian
sudo apt-get install jq
```

### Adding New Test Data

When adding new test data from the real Errata Tool API:

1. Place the raw JSON files in the appropriate directory structure:
   ```
   errata-responses/{type}/example-advisory-N/{advisoryId}_{endpoint}.json
   ```

2. Run the anonymization script:
   ```bash
   ./hack/anonymize-test-data.sh
   ```

3. Verify the anonymization worked correctly by checking:
   - No real user names, emails, or IDs remain
   - No real signing keys remain
   - CVE references are anonymized
   - Test data structure is preserved

### What Is NOT Anonymized

The following data is preserved as it's needed for testing:
- Advisory IDs and numbers
- Build IDs and NVRs (Name-Version-Release)
- Product names and versions
- Advisory status and dates
- Build architecture information
- File names and paths
- Boolean flags and status fields
- Advisory content (synopsis, description, solution)

This ensures the test data remains realistic and useful for testing while removing sensitive information.