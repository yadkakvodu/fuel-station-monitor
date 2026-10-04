import http from 'k6/http';

export const options = {
    vus: 50,
    duration: '10s',
};

export default function () {
    http.get('http://localhost:8080/api/stations/1/fuel');
}