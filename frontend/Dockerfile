# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: BUILD
# Node.js is needed to run the Angular CLI and compile TypeScript to JavaScript.
# Like the backend Dockerfile, this stage is discarded after compilation —
# Node.js and all node_modules will NOT be in the final image.
# ─────────────────────────────────────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package.json and the lock file BEFORE copying source code.
# Same layer caching trick as the backend: if dependencies haven't changed,
# Docker reuses the cached node_modules layer and skips npm install.
COPY package.json pnpm-lock.yaml .npmrc ./

# Install pnpm globally, then install project dependencies.
# --frozen-lockfile: fail if pnpm-lock.yaml would need to be updated.
# This prevents silent dependency upgrades in CI — what you test locally
# is exactly what gets deployed.
RUN npm install -g pnpm && pnpm install --frozen-lockfile --shamefully-hoist --ignore-scripts=false

# Copy all source files and run the production build.
# Angular CLI compiles TypeScript, bundles assets, and outputs static files
# to /app/dist/frontend/browser — just HTML, CSS, and JavaScript.
COPY . .
RUN pnpm build

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: SERVE
# nginx is a high-performance web server purpose-built for serving static files.
# The final image is tiny — nginx:alpine is only ~25 MB.
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:alpine

# Copy the compiled Angular app from the build stage into nginx's web root.
# nginx will serve these files directly to the browser.
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Replace the default nginx config with our custom one.
# The custom config handles Angular Router and proxies /api calls to the backend.
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
