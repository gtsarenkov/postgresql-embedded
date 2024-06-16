# Introduction
Container-independent framework to run PostgreSQL as embedded. Primarily, for the purpose of ad-hoc unit/integration/stress testing.

# Alternative solutions
There are alternative solutions exists, just search the internet.

# Requirements
Internet connection or Reverse Proxy (like Nginx) with cached database to handle database distribution downloads.

# Workflow
- Download distribution into cache or return cached distribution
- Extract distribution into shared folder or returned path of shared folder.
- Start database with generated or provided data directory
- Monitor database process for be running - track of database is running.
- Stop database
- Clean data folder
- Clean cached shared folder
- Remove cached distribution 

# Technologies
- Gradle 8.8
- started with JDK 21 (no need to support earlier version)
- Integration with tests for JUnit 4 and JUnit 5.
- PostgreSQL 16.3-2.
- de.flapdoodle.embed.process 4.11.0 to make embeddable distribution.

# History
For change log check Git

2024-06-16 [proof-of-concept] Skeleton creation
