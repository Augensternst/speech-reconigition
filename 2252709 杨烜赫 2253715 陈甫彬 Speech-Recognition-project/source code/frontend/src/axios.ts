import axios from "axios";
import {useAuth} from "./composables/auth.ts";
const axiosInstance = axios.create({
  baseURL: "http://127.0.0.1:3001",
  timeout: 30000,
});

// Add a request interceptor
axiosInstance.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    const auth = useAuth();
    const token = auth.getToken();
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error);
  }
);

// Add a response interceptor
axiosInstance.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
      if(response.data.code === 401) {
            console.log("Unauthorized")
            const auth = useAuth();
            auth.logout();
            window.location.href = "/login";
            return Promise.reject(response);
      }
      let bearerToken = response.headers['Authorization']
        if (bearerToken) {
            let token = bearerToken.split(' ')[1]
            console.log("old token:", useAuth().getToken())
            console.log("new token:", token)
            const auth = useAuth();
            auth.setToken(token);
        }
    return response;
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error);
  }
);

export default axiosInstance;
