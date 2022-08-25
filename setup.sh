# build images
docker build -t modelfun/front modelfun/frontend
docker build -t modelfun/backend modelfun/backend
docker build -t modelfun/alg modelfun/algorithm/torch_backend
docker build -t modelfun/paddlealg modelfun/algorithm/paddle_backend

# run application
docker compose -f docker-compose.yaml up -d