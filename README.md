# filevscan
This repository contains a restfull services providing a FileScanner Resource. Leveraging on clamAV, this service scans
input files for viruses.

Restfull service is implemented through SpringBoot.

Docker Compose is used to provide a full services, with one container providing rest services and the other one, linked 
to services, providing clamav - clamd daemon. 
According to the documentation, this clamd dock provides automatic virus data update.
