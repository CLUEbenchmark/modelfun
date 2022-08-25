FROM node:14.16.0 AS front_base
COPY . /opt/app/
WORKDIR /opt/app/
RUN npm config set registry https://registry.npm.taobao.org
RUN npm install \
    && npm run build
FROM nginx
COPY --from=front_base /opt/app/dist /usr/share/nginx/html
COPY default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80