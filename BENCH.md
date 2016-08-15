```bash
# 2,7 GHz Intel Core i5 Macbook 2016.08.15

wrk -t 4 -c 100 -d 10s http://localhost:8081/template-1
Running 10s test @ http://localhost:8081/template-1
  4 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    22.32ms   32.13ms 320.32ms   94.86%
    Req/Sec     1.51k   318.77     1.96k    90.77%
  59757 requests in 10.07s, 64.97MB read
Requests/sec:   5935.25
Transfer/sec:      6.45MB
```
