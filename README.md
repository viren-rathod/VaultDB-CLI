# VaultDB CLI

VaultDB CLI is a Spring Boot-based command-line tool designed to automate database backups and restoration. It supports multiple database types and storage backends, making it easy to secure your data locally or in the cloud.

## Features

* **Multi-Database Support**: Backup and Restore for **MySQL** and **PostgreSQL**.
* Save backups to **Local File System** or **AWS S3**.
* **Restore Capability**: Easily restore databases from local or remote backup files.
* **Scheduling Helper**: Generates Cron or Windows Task Scheduler commands for you.
* **Secure**: Supports environment variable-based password handling.

---

## Prerequisites

* **Java 17** or higher.
* **Maven** (for building the project).
* **Database Tools**: The machine running this tool must have the native database CLI tools installed and available in the system `PATH`:
    * `mysqldump` & `mysql` (for MySQL)
    * `pg_dump` & `psql` (for PostgreSQL)

---

## Configuration

### 1. Storage Configuration

**Option A: Local Storage (Default)**
```properties
storage.type=local
storage.local.dir=./backups
```

**Option B: AWS S3 Storage**

```properties
storage.type=s3
storage.s3.bucket=my-backup-bucket
storage.s3.region=us-east-1
```

### 2. Notifications (Optional)

To enable Slack notifications, add your webhook URL:

```properties
slack.webhook.url=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

### 3. CLI Mode
Ensure the web environment is disabled to run as a pure CLI tool and avoid "Unable to create system terminal" errors:

```properties
spring.main.web-application-type=none
```

---

## Installation & Build

1. **Clone the repository**:
```bash
git clone [https://github.com/viren-rathod/VaultDB-CLI.git](https://github.com/viren-rathod/VaultDB-CLI.git)
cd vaultdb-cli
```


2. **Build the JAR**:
```bash
mvn clean package
```



---

## Usage

To start the interactive shell, run the generated JAR file from your terminal:

```bash
java -jar target/vaultdb-cli-0.0.1-SNAPSHOT.jar
```

You will see the prompt: `shell:>`

### 1. Create a Backup

**Syntax:**

```bash
backup create --type <db_type> --host <host> --user <user> --password <pass> --dbName <db_name> --storage <local|s3>
```

**Examples:**

* **MySQL to Local Storage:**
```bash
backup create --type mysql --host localhost --user root --password secret --dbName my_app_db --storage local
```


* **Postgres to S3:**
```bash
backup create --type postgres --host localhost --port 5432 --user postgres --password secret --dbName user_db --storage s3
```



### 2. Restore a Database

**Syntax:**

```bash
backup restore --type <db_type> --host <host> --user <user> --password <pass> --dbName <target_db> --file-path <path_to_backup>
```

**Example:**

```bash
backup restore --type mysql --host localhost --user root --password secret --dbName my_app_db --file-path backups/my_app_db/backup_20230101.sql
```

### 3. Scheduling Help

Generate the command needed to run backups automatically via Cron (Linux) or Task Scheduler (Windows).

```bash
backup schedule --cron "0 2 * * *" --command "backup create --type mysql --dbName mydb ..."
```

Project: https://github.com/viren-rathod/VaultDB-CLI
