# KeyLock CI/CD Pipeline

Complete GitHub Actions setup for development (dev), testing (qa), and production (main) branches.

## Quick Start

### 1. Push to GitHub
```bash
git add .
git commit -m "Add CI/CD pipeline"
git push origin dev
```

### 2. Set Up GitHub Secrets (Production Only)
Go to GitHub Repo → **Settings** → **Secrets and variables** → **Actions**

Add these 4 secrets for production signing:
```
SIGNING_KEY          = Base64 encoded keystore
KEY_ALIAS            = Your key alias
KEYSTORE_PASSWORD    = Your keystore password
KEY_PASSWORD         = Your key password
```

**Generate signing key:**
```bash
keytool -genkey -v -keystore keylock.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias keylock-prod-key

# Encode to Base64 (Windows):
$bytes = [System.IO.File]::ReadAllBytes("keylock.jks")
$base64 = [System.Convert]::ToBase64String($bytes)
$base64 | clip
```

### 3. Start Development
```bash
# Create feature branch from dev
git checkout -b feature/your-feature

# Make changes, commit, push
git push origin feature/your-feature

# Create Pull Request to dev
# GitHub Actions automatically builds and tests
```

## Branch Strategy

### `dev` Branch (Development)
- **Build Type:** Debug APK (fast builds, 5-8 min)
- **Triggers:** Push, Pull Requests
- **Artifacts:** 30-day retention
- **Use Case:** Daily development, rapid testing
- **Auto-runs:** Lint, unit tests, debug build

### `qa` Branch (Quality Assurance)
- **Build Type:** Release APK (optimized, 12-15 min)
- **Triggers:** Push, Pull Requests
- **Artifacts:** 30-day retention
- **Use Case:** QA team testing with production-like build
- **Auto-runs:** Lint, unit tests, release build, code quality

### `main` Branch (Production)
- **Build Type:** Signed Release APK + AAB (20 min)
- **Triggers:** Push (tag or direct commit)
- **Artifacts:** 90-day retention
- **Use Case:** Production releases for Play Store/App Store
- **Auto-runs:** Full build, signing, quality gate, release notes

## Workflow Overview

```
┌─────────────────────────────────────────────────────┐
│         LOCAL DEVELOPMENT (dev branch)              │
│  $ git checkout feature/xyz                          │
│  $ ... code changes ...                              │
│  $ git push origin feature/xyz                       │
└───────────────────┬─────────────────────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │  Pull Request to dev │
         └─────────┬────────────┘
                   │
        ┌──────────▼──────────┐
        │  GitHub Actions     │
        │  ✓ Lint checks     │
        │  ✓ Compile         │
        │  ✓ Run tests       │
        │  ✓ Build debug APK │
        │  (5-8 min)         │
        └─────────┬──────────┘
                  │
        ┌─────────▼──────────────┐
        │ Review & Merge to dev  │
        └─────────┬──────────────┘
                  │
                  ▼
    ┌──────────────────────────────┐
    │  QA TESTING (qa branch)      │
    │  $ git merge dev → qa        │
    │  $ git push origin qa        │
    └─────────┬────────────────────┘
              │
    ┌─────────▼──────────────┐
    │  GitHub Actions        │
    │  ✓ Build release APK   │
    │  ✓ Run tests           │
    │  ✓ Lint & quality      │
    │  (12-15 min)           │
    └─────────┬──────────────┘
              │
    ┌─────────▼──────────────────┐
    │ QA downloads & tests APK   │
    │ (production-ready build)   │
    └─────────┬──────────────────┘
              │
                  ▼
    ┌──────────────────────────────┐
    │  PRODUCTION (main branch)    │
    │  $ git merge qa → main       │
    │  $ git tag v1.0.0            │
    │  $ git push origin v1.0.0    │
    └─────────┬────────────────────┘
              │
    ┌─────────▼──────────────────┐
    │  GitHub Actions            │
    │  ✓ Build release           │
    │  ✓ Build AAB (App Bundle)  │
    │  ✓ Sign artifacts          │
    │  ✓ Quality gate tests      │
    │  ✓ Generate release notes  │
    │  (20 min)                  │
    └─────────┬──────────────────┘
              │
    ┌─────────▼──────────────────────┐
    │  Production Artifacts Ready:   │
    │  - Signed APK (for manual)     │
    │  - AAB (for Play Store)        │
    │  - Release notes               │
    └────────────────────────────────┘
```

## Build Matrix

All branches build in parallel using GitHub Actions matrix strategy:

| Branch | Build Type | Output | Time |
|--------|-----------|--------|------|
| dev | debug | APK | 5-8 min |
| qa | release | APK | 12-15 min |
| main | release + sign | APK + AAB | 20 min |

## Workflows

### `build.yml` - Main Pipeline
Runs on all three branches (dev, qa, main)

**Jobs:**
- **build** - Compiles APK based on branch (debug or release)
- **lint** - Code quality, lint checks
- **test** - Unit tests and test reports

**Artifacts:**
- APK files
- Lint reports
- Test results

### `code-quality.yml` - Code Analysis
Runs on all three branches

**Jobs:**
- **code-quality** - Comprehensive code analysis
- **dependency-check** - Security & dependency analysis

### `production-release.yml` - Production Only
Runs only on main branch (push or tags starting with `v`)

**Jobs:**
- **production-build** - Release build, signing, AAB generation
- **quality-gate** - Quality validation, test coverage, release notes

## Accessing Artifacts

1. Go to GitHub repository → **Actions** tab
2. Click the workflow run (shows branch and timestamp)
3. Scroll to bottom → "Artifacts" section
4. Download artifact (APK for dev/qa, APK+AAB for main)

**Download links expire after retention period (30-90 days)**

## Common Workflows

### Merge to QA for Testing
```bash
git checkout qa
git pull origin qa
git merge dev
git push origin qa

# Actions builds release APK
# Go to Actions → wait for build → download APK
```

### Release to Production
```bash
git checkout main
git pull origin main
git merge qa
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# Actions builds, signs, generates AAB
# Download artifacts for distribution
```

### Rollback/Rebuild
```bash
# Re-run workflow from GitHub Actions UI
# Or push new commit to trigger rebuild
git commit --allow-empty -m "Rebuild"
git push origin main
```

## Troubleshooting

### Build Fails
1. Check **Actions** tab → workflow run → scroll logs
2. Common issues:
   - Gradle sync problem → Run locally: `./gradlew clean build`
   - Test failure → Fix tests before push
   - Lint errors → Fix code before merge

### Signing Fails (main branch)
- Verify **SIGNING_KEY** is valid Base64
- Check **KEY_ALIAS** exists in keystore
- Validate **KEYSTORE_PASSWORD** and **KEY_PASSWORD**
- Test locally: `./gradlew assembleRelease`

### APK Not Generated
- Check build logs for compilation errors
- Ensure JDK 21+ installed locally
- Verify `local.properties` has correct SDK path

### Artifacts Not Available
- Workflow may still be running → wait for completion
- Check retention period (30 days = automatic deletion)
- Re-run workflow to generate new artifacts

## Best Practices

1. **Always merge to dev first** - Test in dev before QA
2. **Use meaningful commit messages** - Helps with tracking
3. **Tag releases** - Use semantic versioning (v1.0.0)
4. **Review CI logs** - Check for warnings/issues
5. **Keep main stable** - Only merge tested, working code
6. **Rotate signing keys** - Do this periodically for security

## Advanced Setup

### Slack Notifications (Optional)
Add to any workflow YAML:
```yaml
  - name: Notify Slack
    if: always()
    uses: slackapi/slack-github-action@v1
    with:
      webhook-url: ${{ secrets.SLACK_WEBHOOK }}
```

### Auto-Deploy to Play Store (Future)
Can add Firebase App Distribution or Play Store publishing step:
```yaml
  - name: Upload to Play Store
    uses: r0adkll/gradle-play-publisher@v3
    with:
      serviceAccountJsonPlainText: ${{ secrets.PLAY_STORE_KEY }}
```

### Auto-Versioning (Future)
Update version automatically on releases:
```yaml
  - name: Bump Version
    uses: actions/setup-node@v3
    run: npm version patch
```

## Support

- **CI/CD Docs:** This file (CI_CD.md)
- **Main Project:** See [README.md](README.md)
- **Full Docs:** See [KEYLOCK_PRO_DOCUMENTATION.md](KEYLOCK_PRO_DOCUMENTATION.md)

---

**KeyLock** - All workflows configured and ready to use!
