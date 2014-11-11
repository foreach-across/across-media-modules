# Across FileManagerModule

## Description
The FileManagerModule provides a central access point for managing different file repositories.  Other modules
persisting files using the FileManager services can get their file repositories managed through this module,
without the need for knowing inner working of file peristence.

## Features
* Simple file system based repository
* Database backed storage - allowing easy migration of files or subsets of files

TODO:
* Amazon S3 storage support
* Hashbased optimization of unique files

## Dependencies

## Settings

## Usage
### Quick start

### Defining a FileRepositoryFactory
### Registering a FileRepository
