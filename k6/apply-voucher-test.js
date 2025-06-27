import http from "k6/http";
import { check } from "k6";

//export let options = {
//  vus: 200,
//  duration: "1s",
//  rps: 200,
//};

export let options = {
  vus: 2,
  duration: "5s",
  rps: 2,
};

export default function () {
  const url = "http://localhost:8080/api/v1/coupons/apply";

  const payload = JSON.stringify({
    orderAmount: 200000,
    couponCode: "COUP838",
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
  };

  const response = http.post(url, payload, params);

  check(response, {
    "status is 200": (r) => r.status === 200,
    "response has data": (r) => r.json("data") !== null,
    "discount amount exists": (r) => r.json("data.discountAmount") !== null,
  });

  console.log(`Response: ${response.body}`);
}
