cp Dockerfile.base Dockerfile && \
./command2label.py command.json >> Dockerfile && \
docker build -t xnat/data-snapshot:0.4.1 -t xnat/data-snapshot:latest .
rm Dockerfile 
