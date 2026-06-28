# Errata Tool API Response Test Data

This directory contains **anonymized** mock JSON responses from the Errata Tool API for testing purposes.

⚠️ **IMPORTANT**: Care has been taken to anonymise test data in this directory has. See [ANONYMIZATION.md](ANONYMIZATION.md) for details.

## Directory Structure

```
errata-responses/
├── container/                    # Container/Docker image advisories
│   ├── example-advisory-1/
│   │   ├── 157826_errata.json   # Erratum details
│   │   └── 157826_builds.json   # Build list
│   ├── example-advisory-2/
│   │   ├── 152887_errata.json
│   │   └── 152887_builds.json
│   └── example-advisory-3/
│       ├── 157751_errata.json
│       └── 157751_builds.json
├── rpm/                         # RPM package advisories
│   ├── example-advisory-1/
│   │   ├── 789012_errata.json
│   │   └── 789012_builds.json
│   └── example-advisory-2/
│       ├── 157548_errata.json
│       └── 157548_builds.json
└── textonly/                    # Text-only advisories (no builds)
    ├── example-advisory-1/
    │   ├── 155577_errata.json
    │   └── 155577_builds.json
    └── example-advisory-2/
        ├── 157587_errata.json
        └── 157587_builds.json
```

## File Naming Convention

Files follow the pattern: `{advisory_id}_{endpoint}.json`

- `{advisory_id}_errata.json` - Response from `/api/v1/erratum/{id}` endpoint
- `{advisory_id}_builds.json` - Response from `/api/v1/erratum/{id}/builds_list` endpoint

## Adding New Test Data

To add real API responses:

1. Create a new directory under the appropriate type (container/rpm/textonly)
   ```bash
   mkdir -p src/test/resources/errata-responses/container/example-advisory-4
   ```

2. Fetch the API responses with proper naming:
   ```bash
   ADVISORY="123456"
   cd src/test/resources/errata-responses/container/example-advisory-4

   # Fetch errata details
   curl -H 'Accept: application/json' --negotiate -u : \
     "https://errata.engineering.redhat.com/api/v1/erratum/${ADVISORY}" | \
     jq > ${ADVISORY}_errata.json

   # Fetch builds list
   curl -H 'Accept: application/json' --negotiate -u : \
     "https://errata.engineering.redhat.com/api/v1/erratum/${ADVISORY}/builds_list" | \
     jq > ${ADVISORY}_builds.json
   ```

3. **IMPORTANT**: Anonymize the data before committing:
   ```bash
   cd /path/to/project/root
   ./hack/anonymize-test-data.sh
   ```

4. Update the test in `ErrataToolServiceTest.java` to include the new test case in the `advisoryTestData()` method

## Data Anonymization

Out of caution data is anonymized using the script: `hack/anonymize-test-data.sh`

**What gets anonymized:**
- User IDs, names, emails, and organization IDs
- Signing keys (sig_key, container_sig_key)
- Reporter, assignee, package owner, and manager IDs
- CVE references (replaced with CVE-XXXX-XXXXX)
- Kerberos principal information

**What is preserved:**
- Advisory IDs and structure
- Build IDs and NVRs
- Product names and versions
- Advisory status and dates
- File names and architectures

See [ANONYMIZATION.md](ANONYMIZATION.md) for complete details.

## Test Integration

The test class `ErrataToolServiceTest` uses JUnit 5 parameterized tests to load these JSON files and mock the `ErrataClient` responses. This allows tests to run without requiring Kerberos authentication or network access to the actual Errata Tool service.

The helper method `loadJsonResponse(type, descriptiveName, advisoryId, endpoint, clazz)` automatically constructs the correct file path based on the directory structure and naming convention.

### Running Tests

```bash
# Run all tests
mvn test

# Run only the ErrataToolService tests
mvn test -Dtest=ErrataToolServiceTest
```
