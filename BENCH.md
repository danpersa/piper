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

# 2,3 GHz Quad Intel Core i7 Macbook 2016.09.03
# old http.async.client

wrk -c 100 -t 4 -d 10s http://localhost:8081/template-1
Running 10s test @ http://localhost:8081/template-1
  4 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    26.78ms   67.23ms 542.88ms   95.72%
    Req/Sec     1.88k   389.02     7.02k    93.18%
  72170 requests in 10.10s, 78.47MB read
Requests/sec:   7144.36
Transfer/sec:      7.77MB

# new core.async.http.client
wrk -c 100 -t 4 -d 10s http://localhost:8081/template-1
Running 10s test @ http://localhost:8081/template-1
  4 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    14.17ms    4.95ms  75.68ms   95.20%
    Req/Sec     1.80k   204.69     2.46k    87.00%
  72248 requests in 10.07s, 78.55MB read
Requests/sec:   7174.40
Transfer/sec:      7.80MB
```
