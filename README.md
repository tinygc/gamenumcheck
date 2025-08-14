# GameNumCheck

Android number guessing game built with Clean Architecture.

## Overview

A fun number guessing game where players try to guess a randomly generated number between 1-100. The app provides hints and tracks achievements to enhance the gaming experience.

## Architecture

This project follows Clean Architecture principles with:
- **Domain Layer**: Business logic, entities, and use cases
- **Data Layer**: Repository implementations and data sources
- **Presentation Layer**: UI components and ViewModels

## Tech Stack

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Clean Architecture**: Project structure
- **TDD**: Test-driven development approach

## Features

- Number guessing game (1-100)
- Smart hint system
- Achievement tracking
- Clean, modern UI with Jetpack Compose
- Comprehensive unit tests

## Development

The project follows TDD (Test-Driven Development) principles and maintains high code quality with comprehensive testing.

### Notification System

This project includes a Windows Toast notification system for development workflow:

```powershell
powershell -ExecutionPolicy Bypass -File "notify.ps1" -Message "Your message" -Title "Title" -Type "Information"
```

## Repository Structure

This is part of a multi-repository setup:
- **gamenumcheck**: This Android game project
- **settings**: Development configuration and Claude Code settings