# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app

# Leverage Docker layer caching: install dependencies before copying source.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# --- Runtime stage ---
FROM nginx:1.27-alpine
COPY --from=build /app/dist/flooring-finance-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
