---
title: Query4J
description: A Jakarta-native, SQL-first, record-friendly JDBC toolkit
author: RedBeardCodes
tags: ["java", "jakarta", "jdbc", "sql"]
---

# Welcome to Query4J

## Goals
- Jakarta CDI integration
- SQL-first API
- JDBC with minimal abstraction
- Java records as entities
- Explicit SQL over generated SQL
- Small dependency footprint
- Transactio-friendly
- Predictable performance

## Explicit non-goals
- ORM
- entity state tracking
- lazy loading
- query DSL
- automatic schema generation
- relationship management
- criteria builders
- JPQL equivalent

## Philosophy

Query4J is not an ORM.

If you know SQL, you already know Query4J.

The framework never generates SQL, never hides database behavior, and never attempts to synchronize object graphs. It exists to remove JDBC boilerplate, not SQL.
