# Across FileManagerModule

## Description
The FileManagerModule provides a central access point for managing different file repositories.  Other modules
persisting files using the FileManager services can get their file repositories managed through this module,
without the need for knowing inner working of file peristence.

## Features
* Simple file system based repository

TODO:

* Database backed storage - allowing easy migration of files or subsets of files
* Amazon S3 storage support
* Hashbased optimization of unique files

## Dependencies

## Settings
temp folder
default factory root
## Usage
### Quick start
set property for default factory, set property for temp folder
### Registering a FileRepository
register a filerepository from another module, return the delegate
### Defining a FileRepositoryFactory
register a factory for autocreation of file repository


