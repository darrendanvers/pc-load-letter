# Build the Docker image.
docker build -t pc-load-letter-db:1.0 .

# Run it, exsposing the PostgreSQL port.
docker run --rm -p 5432:5432 --env POSTGRES_DB=pc-load-letter-db \
--env POSTGRES_USER=postgres --env POSTGRES_PASSWORD=p0stgr@s \
pc-load-letter-db:1.0
