import axios from 'axios';
import config from './config';

const axiosInstance = axios.create({
  baseURL: process.env.REACT_APP_DOORS_BACKEND_URL,
  timeout: 2000,
  validateStatus: status => status === 200,
});

export const getRoomInfo = async roomName => new Promise((resolve, reject) => {
  axiosInstance.get(`/info/${roomName}`).then(response => {
    resolve(response.data);
  }).catch(error => {
    reject(error);
  });
});

export const getSuggestions = async (options = {}) => new Promise((resolve, reject) => {
  axiosInstance.get('/suggestions', {
    params: {
      number: options.number ?? 1000,
      minSeats: options.minSeats ?? 4,
      minTime: options.minTime ?? 45,
      from: options.from ?? Date.now(),
      equipment: options.equipment ?? [],
    }
  }).then(response => {
    resolve(response.data);
  }).catch(error => {
    reject(error);
  });
});
