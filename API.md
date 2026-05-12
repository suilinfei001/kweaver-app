# Sherpa-ONNX ASR Service API 文档

## 服务概述

基于 Paraformer 的中文流式语音识别服务，支持中英文双语识别。

- **Base URL**: `http://localhost:8348`
- **服务版本**: 1.0.0
- **模型**: sherpa-onnx-streaming-paraformer-bilingual-zh-en

---

## 接口列表

| 接口 | 方法 | 描述 |
|------|------|------|
| `/health` | GET | 健康检查 |
| `/asr` | POST | 识别 WAV 音频文件 |
| `/asr/raw` | POST | 识别原始 PCM 音频 |

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
  "model_path": "/app/models/sherpa-onnx-streaming-paraformer-bilingual-zh-en"
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
curl http://localhost:8348/health
```

---

## 2. 识别 WAV 音频

识别 WAV 格式的音频文件，自动处理重采样和声道转换。

### 请求

```http
POST /asr
Content-Type: multipart/form-data
```

### 入参

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| `file` | file | form-data | ✅ | WAV 音频文件 |

### 支持的音频格式

- 采样率: 8000Hz / 16000Hz (自动重采样)
- 声道: 单声道 / 双声道 (自动转换为单声道)
- 位深: 16-bit / 32-bit

### 响应

```json
{
  "text": "识别的文字内容",
  "success": true,
  "error": null
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `text` | string | 识别出的文字，失败时为空字符串 |
| `success` | boolean | 是否识别成功 |
| `error` | string \| null | 错误信息，成功时为 null |

### 错误响应

```json
{
  "text": "",
  "success": false,
  "error": "Only WAV files are supported"
}
```

### 示例

```bash
# 识别本地 WAV 文件
curl -X POST "http://localhost:8348/asr" \
  -F "file=@/path/to/audio.wav"

# 使用测试音频
curl -X POST "http://localhost:8348/asr" \
  -F "file=@./test_wavs/8k.wav"
```

### Python 示例

```python
import requests

url = "http://localhost:8348/asr"
files = {"file": open("audio.wav", "rb")}

response = requests.post(url, files=files)
print(response.json())

# 输出: {"text": "识别结果", "success": true, "error": null}
```

---

## 3. 识别原始 PCM 音频

识别原始 PCM 音频数据（16-bit 有符号整数）。

### 请求

```http
POST /asr/raw?sample_rate=16000
Content-Type: multipart/form-data
```

### 入参

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| `file` | file | form-data | ✅ | 原始 PCM 音频数据 |
| `sample_rate` | int | query | ❌ | 采样率，默认 16000 |

### 音频格式要求

- 采样率: 与 `sample_rate` 参数一致
- 位深: 16-bit 有符号整数 (int16)
- 声道: 单声道

### 响应

```json
{
  "text": "识别的文字内容",
  "success": true,
  "error": null
}
```

### 响应字段说明

同 `/asr` 接口。

### 示例

```bash
# 识别 16kHz 原始 PCM 音频
curl -X POST "http://localhost:8348/asr/raw?sample_rate=16000" \
  -F "file=@audio.pcm"

# 识别 8kHz 原始 PCM 音频
curl -X POST "http://localhost:8348/asr/raw?sample_rate=8000" \
  -F "file=@audio_8k.pcm"
```

### Python 示例

```python
import requests
import numpy as np
import io

# 生成测试音频 (如果是实际应用，读取真实 PCM 文件)
sample_rate = 16000
duration = 1  # 1秒
samples = np.random.randint(-32768, 32767, sample_rate * duration, dtype=np.int16)

# 转换为字节
pcm_data = samples.tobytes()

url = "http://localhost:8348/asr/raw"
params = {"sample_rate": sample_rate}
files = {"file": ("audio.pcm", pcm_data, "application/octet-stream")}

response = requests.post(url, params=params, files=files)
print(response.json())
```

---

## 错误码

| HTTP 状态码 | 说明 |
|--------------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 (如文件格式不支持) |
| 503 | 服务不可用 (模型未加载) |

---

## 完整使用示例

### JavaScript (Node.js)

```javascript
const FormData = require('form-data');
const fs = require('fs');
const axios = require('axios');

async function recognizeAudio(filePath) {
  const form = new FormData();
  form.append('file', fs.createReadStream(filePath));

  const response = await axios.post('http://localhost:8348/asr', form, {
    headers: form.getHeaders()
  });

  return response.data;
}

// 使用
recognizeAudio('./test.wav')
  .then(result => {
    if (result.success) {
      console.log('识别结果:', result.text);
    } else {
      console.error('识别失败:', result.error);
    }
  })
  .catch(err => console.error('请求错误:', err));
```

### Java

```java
import java.io.File;
import java.http.HttpClient;
import java.http.MultipartBody;
import java.http.RequestBody;
import java.nio.file.Files;
import java.net.URI;

public class AsrClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        File file = new File("audio.wav");
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(),
                RequestBody.create(Files.readAllBytes(file.toPath()),
                    org.http4s.MediaType.applicationOctetStream))
            .build();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8348/asr"))
            .POST(requestBody)
            .build();

        java.net.http.HttpResponse<String> response =
            client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }
}
```

---

## Android 按住说话场景

适用于 Android App 的「按住录音按钮说话」场景：用户按住录音 → 松开发送 → 获取识别结果。

### 交互流程

```
用户按住按钮 → 开始录音 → [用户松开按钮] → 停止录音 → 发送音频 → ASR识别 → 显示结果
```

### 录音工具类

```kotlin
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.ByteArrayOutputStream

class AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val pcmData = ByteArrayOutputStream()

    fun startRecording(): Boolean {
        if (isRecording) return false

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            return false
        }

        isRecording = true
        audioRecord?.startRecording()
        return true
    }

    fun stopRecording(): ByteArray {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        return pcmData.toByteArray()
    }

    fun readAudio() {
        val buffer = ByteArray(bufferSize)
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
            if (read > 0) {
                pcmData.write(buffer, 0, read)
            }
        }
    }
}
```

### ASR 客户端

```kotlin
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ASRClient(private val baseUrl: String = "http://YOUR_SERVER_IP:8348") {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun recognize(pcmData: ByteArray, sampleRate: Int = 16000): ASRResult {
        val wavData = pcmToWav(pcmData, sampleRate)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "audio.wav",
                wavData.toRequestBody("audio/wav".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/asr")
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            parseResponse(body)
        }
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int): ByteArray {
        val os = ByteArrayOutputStream()
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * 2

        os.write("RIFF".toByteArray())
        os.write(intToByteArray(totalDataLen))
        os.write("WAVE".toByteArray())
        os.write("fmt ".toByteArray())
        os.write(intToByteArray(16))
        os.write(shortToByteArray(1))
        os.write(shortToByteArray(1))
        os.write(intToByteArray(sampleRate))
        os.write(intToByteArray(byteRate))
        os.write(shortToByteArray(2))
        os.write(shortToByteArray(16))
        os.write("data".toByteArray())
        os.write(intToByteArray(pcmData.size))
        os.write(pcmData)

        return os.toByteArray()
    }

    private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToByteArray(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte()
    )

    private fun parseResponse(json: String): ASRResult {
        // 使用 JSON 解析库解析响应
        // 返回 ASRResult(text, success, error)
    }
}

data class ASRResult(
    val text: String,
    val success: Boolean,
    val error: String?
)
```

### 录音按钮 Activity

```kotlin
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class VoiceActivity : AppCompatActivity() {

    private lateinit var recordBtn: Button
    private lateinit var resultText: TextView
    private val audioRecorder = AudioRecorder()
    private val asrClient = ASRClient("http://192.168.1.100:8348")

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        recordBtn = findViewById(R.id.recordBtn)
        resultText = findViewById(R.id.resultText)

        recordBtn.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (audioRecorder.startRecording()) {
                        scope.launch { audioRecorder.readAudio() }
                        recordBtn.text = "录音中..."
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    recordBtn.text = "识别中..."
                    val pcmData = audioRecorder.stopRecording()
                    scope.launch { recognizeAudio(pcmData) }
                    true
                }
                else -> false
            }
        }
    }

    private suspend fun recognizeAudio(pcmData: ByteArray) {
        withContext(Dispatchers.IO) {
            val result = asrClient.recognize(pcmData)
            withContext(Dispatchers.Main) {
                if (result.success) {
                    resultText.text = result.text
                } else {
                    resultText.text = "识别失败: ${result.error}"
                }
                recordBtn.text = "按住说话"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
```

### 布局文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center">

    <TextView
        android:id="@+id/resultText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="请按住按钮说话"
        android:textSize="18sp"/>

    <Button
        android:id="@+id/recordBtn"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginTop="32dp"
        android:background="@drawable/record_button_bg"
        android:text="按住说话"/>

</LinearLayout>
```

### 权限配置

在 `AndroidManifest.xml` 中添加权限：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

### 连接地址说明

| 运行环境 | 服务器地址 |
|----------|------------|
| Android 模拟器 | `http://10.0.2.2:8348` |
| 真机调试 | `http://<电脑IP>:8348` (需确保在同一局域网) |
| 正式发布 | `http://<服务器公网IP>:8348` |

---

## 本地开发

```bash
# 启动服务
cd deployment/asr
docker compose up -d

# 查看日志
docker logs sherpa-onnx-asr -f

# 停止服务
docker compose down
```

---

## 注意事项

1. 首次启动需要加载模型，请等待 `/health` 返回 `model_loaded: true`
2. WAV 文件会自动重采样，支持 8kHz 和 16kHz 音频
3. 建议使用 16kHz 采样率的音频以获得最佳识别效果
4. 长音频识别可能需要较长时间，请耐心等待
