FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine
USER root
WORKDIR /usr/share/nginx/html

COPY --from=build /app/dist/shopapp ./
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /docker-entrypoint.d/40-app-config.sh
# Strip Windows CRLF so shebang is not /usr/bin/env sh^M (exit 127 in container)
RUN sed -i 's/\r$//' /docker-entrypoint.d/40-app-config.sh && chmod 755 /docker-entrypoint.d/40-app-config.sh

# Runtime entrypoint writes /tmp/app-config.js; base image runs as non-root nginx
RUN chown -R nginx:nginx /usr/share/nginx/html

USER nginx

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
