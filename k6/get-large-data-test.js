import http from "k6/http";
import { check } from "k6";

export let options = {
  vus: 10,
  duration: "1s",
  rps: 10,
};

export default function () {
  const url = "http://localhost:8080/api/v1/redis-data/get-large-data/50";

  const response = http.get(url);

  check(response, {
    "status is 200": (r) => r.status === 200,
  });

  console.log(`Response: ${response.body}`);
}
