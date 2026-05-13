# Sherpa-ONNX TTS Service API 文档

## 服务概述

基于 VITS 的中文语音合成服务，支持语速调节。

- **Base URL**: `http://localhost:8349`
- **服务版本**: 1.0.0
- **模型**: vits-zh-hf-fanchen-c (VITS Chinese)

---

## 接口列表

| 接口 | 方法 | 描述 |
|------|------|------|
| `/health` | GET | 健康检查 |
| `/tts` | POST | 文本转语音合成 |

---

## 1. 健康检查

### 请求

```http
GET /health
```

### 响应

```json
{
  "status": "healthy",
  "model_loaded": true,
  "model_path": "/app/models/vits-zh-hf-fanchen-c"
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | 服务状态：`healthy` 或 `unhealthy` |
| `model_loaded` | boolean | 模型是否已加载 |
| `model_path` | string | 模型文件路径 |

### 示例

```bash
curl http://localhost:8349/health
```

---

## 2. 语音合成

将文本转换为语音音频。

### 请求

```http
POST /tts
Content-Type: application/json
```

### 入参

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `text` | string | ✅ | - | 要合成的文本（中文） |
| `speed` | float | ❌ | 1.0 | 语速，1.0 为正常语速 |
| `sid` | int | ❌ | 0 | 说话人 ID |

### 请求示例

```json
{
  "text": "你好，欢迎使用智能购物向导",
  "speed": 1.0,
  "sid": 0
}
```

### 响应

```json
{
  "audio_base64": "UklGRiQAAABX...",
  "sample_rate": 22050,
  "success": true,
  "error": null
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `audio_base64` | string | WAV 音频的 Base64 编码，失败时为空字符串 |
| `sample_rate` | int | 音频采样率（22050 Hz） |
| `success` | boolean | 是否合成成功 |
| `error` | string \| null | 错误信息，成功时为 null |

### 错误响应

```json
{
  "audio_base64": "",
  "sample_rate": 0,
  "success": false,
  "error": "Text cannot be empty"
}
```

### 示例

```bash
curl -X POST http://localhost:8349/tts \
  -H "Content-Type: application/json" \
  -d '{"text": "你好，欢迎使用智能购物向导"}'
```

---

## 部署说明

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TTS_PORT` | `8349` | 服务暴露端口 |

### Docker Compose 部署

```bash
cd deployment/tts
docker compose up -d
```

### 验证部署

```bash
# 检查健康状态
curl http://localhost:8349/health

# 测试语音合成
curl -X POST http://localhost:8349/tts \
  -H "Content-Type: application/json" \
  -d '{"text": "测试语音合成"}'
```

### 注意事项

1. 模型文件存放在宿主机的 `backend/models/vits-zh-hf-fanchen-c` 目录
2. 通过 Docker volume 挂载到容器内 `/app/models`
3. 首次启动会自动下载模型文件（如 volume 中不存在）
4. 默认使用 CPU 推理，配置 4 线程
