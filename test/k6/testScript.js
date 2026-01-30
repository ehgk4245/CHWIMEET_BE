import http from 'k6/http';
import { check, sleep } from 'k6';

const testImage = open('./test-image.jpg', 'b');

const BASE_URL = 'https://api.chwimeet.site';

export const options = {
    scenarios: {
        thread_hog: {
            executor: 'constant-vus',
            exec: 'llmRequest',
            vus: 200,
            duration: '3m',
        },
        normal_user: {
            executor: 'constant-vus',
            exec: 'normalRequest',
            vus: 50,
            duration: '3m',
        },
    },
};

export function llmRequest() {
    const url = `${BASE_URL}/api/v1/posts/gen-detail`;
    const data = {
        images: http.file(testImage, 'test.jpg', 'image/jpeg'),
        additionalInfo: '스레드 고갈 테스트',
    };

    const params = {
        timeout: '120s'
    };

    const res = http.post(url, data, params);

    if (res.status !== 200) {
        console.log(`[!] LLM API Failed | Status: ${res.status} | Body: ${res.body}`);
    }

    check(res, { 'LLM status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function normalRequest() {
    const url = `${BASE_URL}/api/v1/posts?size=30`;

    const res = http.get(url);

    check(res, { 'Normal API status is 200': (r) => r.status === 200 });
    sleep(1);
}