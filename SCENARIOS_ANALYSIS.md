# 📊 Phân Tích So Sánh 4 Kịch Bản Thử Nghiệm

## 🎯 Mục Đích Tổng Quan

Bộ 4 kịch bản được thiết kế để đánh giá toàn diện hiệu suất của các thuật toán lập lịch:
- **Standard Load Balancing**: Baseline comparison
- **Dynamic AV Load Balancing**: Considers historical load
- **Dynamic AV + Aging**: **Prevents starvation** + load balancing + decay history

---

## 📋 Tổng Quan 4 Kịch Bản

| Kịch Bản | Số Cloudlets | Đặc Điểm Chính | Mục Đích Test |
|----------|--------------|----------------|---------------|
| **Balanced Workload** | 60 | Phân bố đều (short/medium/long) | Performance cơ bản, baseline |
| **Bursty Workload** | 80 | 3 đợt burst traffic | Load balancing + prevent starvation |
| **Dynamic Workload** | 100 | Many short, few very long tasks | Starvation prevention + fairness |
| **Heavy Load** | 150 | 70% long tasks, sustained load | Scalability + heavy load handling |

---

## 1️⃣ Balanced Workload Scenario

### 📊 Thông Số Kỹ Thuật

```
Tổng số cloudlets: 60
Phân bố:
  - 20 short tasks  (1-3s)   → arrive 0-4s  (mỗi 0.2s)
  - 20 medium tasks (5-8s)   → arrive 4-8s  (mỗi 0.2s)
  - 20 long tasks   (12-18s) → arrive 8-12s (mỗi 0.2s)

Arrival pattern: Đều đặn, sequential
```

### 🎯 Mục Đích

- **Baseline comparison**: Đánh giá performance với workload cân bằng
- Test khả năng xử lý mixed workload (short + medium + long)
- So sánh cơ bản giữa các thuật toán


---

## 2️⃣ Bursty Workload Scenario

### 📊 Thông Số Kỹ Thuật

```
Tổng số cloudlets: 80
Phân bố theo 3 đợt BURST:

BURST 1 (t=0-1s):
  - 30 short tasks (1-2s)
  - Arrival: Dense (mỗi 0.033s)
  - Purpose: Test initial load balancing pressure

BURST 2 (t=5-6s):
  - 20 VERY LONG tasks (25-35s) 
  - Arrival: Dense (mỗi 0.05s)
  - Purpose: Test STARVATION prevention

BURST 3 (t=10-11s):
  - 30 medium tasks (5-8s)
  - Arrival: Dense (mỗi 0.033s)
  - Purpose: Test fairness khi long tasks đang execute
```

### 🎯 Mục Đích

- **Starvation prevention**: Long tasks từ Burst 2 có nguy cơ đói cao
- Test load balancing dưới áp lực burst traffic
- Test fairness: Tasks từ Burst 3 không được ưu tiên hơn tasks từ Burst 2


---

## 3️⃣ Dynamic Workload Scenario

### 📊 Thông Số Kỹ Thuật

```
Tổng số cloudlets: 100
Phân bố theo 3 phases:

PHASE 1 (t=0-3s):
  - 30 medium tasks (8-12s)
  - Arrival: Dense (mỗi 0.1s)
  - Purpose: Occupy VMs sớm

PHASE 2 (t=3-13s):
  - 50 short tasks (1-3s)
  - Arrival: Continuous (mỗi 0.2s)
  - Purpose: Create continuous pressure

PHASE 3 (scattered t=0-13s):
  - 20 VERY LONG tasks (20-30s)
  - Arrival: Scattered (mỗi 0.7s)
  - Purpose: Test extreme starvation scenario
```

### 🎯 Mục Đích

- **Extreme starvation test**: 20 very long tasks vs 80 short/medium tasks
- Test aging mechanism với continuous pressure từ short tasks
- Test historical load tracking trong môi trường dynamic


---

## 4️⃣ Heavy Load Scenario

### 📊 Thông Số Kỹ Thuật

```
Tổng số cloudlets: 150 (SCALE LỚN!)
Phân bố:

WAVE 1 (t=0-15s):
  - 105 long tasks (15-25s) - 70% workload
  - Arrival: Sustained (mỗi 0.143s)
  - Purpose: Heavy, sustained load

WAVE 2 (t=15-20s):
  - 45 short tasks (2-5s) - 30% workload
  - Arrival: Continuous (mỗi 0.111s)
  - Purpose: Test if short tasks can execute quickly despite heavy load
```

### 🎯 Mục Đích

- **Scalability test**: Xử lý 150 cloudlets
- Test performance dưới sustained heavy load (70% long tasks)
- Test if aging + historical load mechanisms scale well


---

## 🔬 So Sánh Tổng Thể 4 Kịch Bản


### Điểm Khác Biệt Chính

#### 1. **Balanced**: Baseline scenario
- Arrival pattern: Sequential, gradual
- Workload: Mixed but balanced
- Challenge: None special, just basic performance

#### 2. **Bursty**: Starvation focus
- Arrival pattern: **3 bursts**
- Workload: Short → **Very Long** → Medium
- Challenge: Long tasks từ Burst 2 starve if no aging

#### 3. **Dynamic**: Extreme starvation focus
- Arrival pattern: **Continuous short tasks** + scattered long tasks
- Workload: **Many short, few very long**
- Challenge: Continuous pressure từ short tasks → long tasks starve severely

#### 4. **Heavy Load**: Scalability focus
- Arrival pattern: Sustained, heavy
- Workload: **70% long tasks**
- Challenge: Scale (150 cloudlets) + sustained load

---

## 📊 Metrics Comparison Template

Sau khi chạy simulation, compare các metrics sau:

### 1. Makespan (Total execution time)
```
Lower is better
Shows: Overall efficiency
```

### 2. Average Response Time
```
Lower is better
Shows: User experience (submit → finish)
```

### 3. Average Waiting Time
```
Lower is better
Shows: Queue efficiency
```

### 4. Throughput (tasks/second)
```
Higher is better
Shows: System productivity
```

### 5. Fairness (Standard Deviation of Waiting Time)
```
Lower is better
Shows: No task starves
```

---

## 🎓 Kết Luận & Recommendations

### Khi Nào Dùng Từng Kịch Bản?

1. **Balanced Workload** → Quick sanity check, baseline comparison
2. **Bursty Workload** → Test burst handling, initial starvation prevention
3. **Dynamic Workload** → **Main scenario** to prove aging benefits (extreme starvation)
4. **Heavy Load** → Scalability test, production readiness

### Expected Overall Results

```
📌 Hypothesis:
  Dynamic AV + Aging broker sẽ THẮNG ở:
    ✅ Dynamic Workload (extreme starvation prevention)
    ✅ Bursty Workload (burst handling + fairness)
    ✅ Heavy Load (scale + balanced distribution)
    ✅ Balanced Workload (baseline improvement)

📌 Key Improvements:
  1. Starvation Prevention: P(t) = P(0) + α × W(t)
  2. Balanced Load Distribution: H_j(t+) = H_j(t) + T_i,j^exec
  3. No Single VM Overload: Historical load tracking
```

---

## 🔍 Công Thức Áp Dụng

### 1. Dynamic Priority với Aging
```
P(t) = P(0) + α × W(t)

Với:
  - P(0) = L_i / MIPS_j  (execution time estimate)
  - α = 1.0              (aging coefficient)
  - W(t)                 (waiting time at time t)

Ý nghĩa:
  → Task chờ càng lâu, priority càng cao
  → Prevents starvation của long tasks
```

### 2. Historical Load (Standard Accumulation)
```
H_j(t+) = H_j(t) + T_i,j^exec

Với:
  - H_j(t): Current historical load of VM j
  - T_i,j^exec = L_i / MIPS_j    (execution time of task i on VM j)

Ý nghĩa:
  → VM xử lý nhiều task → Load tích lũy cao → Ít được chọn
  → Accumulates execution time history
  → Helps balance load distribution across VMs
```

### 3. Expected Time (for VM selection)
```
ET_j = C_j + H_j(t)

Với:
  - C_j: Current load (đang chạy trên VM j)
  - H_j(t): Historical load (tích lũy từ các tasks đã chạy)

Ý nghĩa:
  → Chọn VM có Expected Time thấp nhất
  → Considers both present and past load
```

---

## 📁 File Structure Reference

```
src/main/java/org/cloudsimplus/
├── scenario/
│   ├── BalancedWorkloadScenario.java     (60 cloudlets)
│   ├── BurstyWorkloadScenario.java       (80 cloudlets)
│   ├── DynamicWorkloadScenario.java      (100 cloudlets)
│   └── HeavyLoadScenario.java            (150 cloudlets)
├── examples/
│   ├── DynamicAvLoadBalancingBroker.java (H_j accumulation)
│   └── DynamicAvAgingLoadAwareBroker.java (H_j + Aging)
├── metrics/
│   └── MetricsCollector.java
├── chart/
│   └── ChartPlotter.java
└── SimulationMain.java
```

