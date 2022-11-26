# The PC LOAD LETTER Database

This directory holds the files to create and initialize a
PostgreSQL database that it used by other projects in this
series.

The [run_db.sh](./run_db.sh) script will build the Docker
container and start it, exposing PostgreSQL's port.

The data in the database will be lost after you shut down
the container.

