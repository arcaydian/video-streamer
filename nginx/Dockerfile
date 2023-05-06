FROM nginx AS proxy
COPY ./default.conf /etc/nginx/conf.d/
CMD ["nginx", "-g", "daemon off;"]