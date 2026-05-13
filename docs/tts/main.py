"""
Standalone Sherpa-ONNX TTS FastAPI Service
基于 VITS 的中文语音合成 API
"""

from __future__ import annotations

import io
import wave
from pathlib import Path
from typing import Optional

import numpy as np
import sherpa_onnx
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel


class TTSRequest(BaseModel):
    text: str
    speed: float = 1.0
    sid: int = 0


class TTSResponse(BaseModel):
    audio_base64: str
    sample_rate: int
    success: bool
    error: Optional[str] = None


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    model_path: str


MODEL_DIR = Path("/app/models/vits-zh-hf-fanchen-c")
MODEL_FILE = MODEL_DIR / "vits-zh-hf-fanchen-C.onnx"
TOKENS_FILE = MODEL_DIR / "tokens.txt"
LEXICON_FILE = MODEL_DIR / "lexicon.txt"
DICT_DIR = MODEL_DIR / "dict"

tts_engine: Optional[sherpa_onnx.OfflineTts] = None


def load_model() -> bool:
    global tts_engine
    if not MODEL_FILE.exists():
        print(f"[TTS] Model file not found: {MODEL_FILE}")
        return False
    if not TOKENS_FILE.exists():
        print(f"[TTS] Tokens file not found: {TOKENS_FILE}")
        return False
    if not LEXICON_FILE.exists():
        print(f"[TTS] Lexicon file not found: {LEXICON_FILE}")
        return False
    if not DICT_DIR.exists():
        print(f"[TTS] Dict directory not found: {DICT_DIR}")
        return False

    try:
        tts_config = sherpa_onnx.OfflineTtsConfig(
            model=sherpa_onnx.OfflineTtsModelConfig(
                vits=sherpa_onnx.OfflineTtsVitsModelConfig(
                    model=str(MODEL_FILE),
                    lexicon=str(LEXICON_FILE),
                    tokens=str(TOKENS_FILE),
                    data_dir=str(DICT_DIR),
                ),
                provider="cpu",
                debug=False,
                num_threads=4,
            ),
            rule_fsts="",
            rule_fars="",
            max_num_sentences=1,
        )
        tts_engine = sherpa_onnx.OfflineTts(tts_config)
        print(f"[TTS] Model loaded successfully from {MODEL_DIR}")
        return True
    except Exception as e:
        print(f"[TTS] Failed to load model: {e}")
        return False


def samples_to_wav_bytes(samples, sample_rate: int) -> bytes:
    if not isinstance(samples, np.ndarray):
        samples = np.array(samples)
    samples_int16 = (samples * 32767).astype("int16")
    wav_buffer = io.BytesIO()
    with wave.open(wav_buffer, 'wb') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        wav_file.writeframes(samples_int16.tobytes())
    return wav_buffer.getvalue()


app = FastAPI(
    title="Sherpa-ONNX TTS Service",
    description="VITS Chinese Text-to-Speech API",
    version="1.0.0"
)


@app.on_event("startup")
async def startup_event():
    load_model()


@app.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse(
        status="healthy" if tts_engine is not None else "unhealthy",
        model_loaded=tts_engine is not None,
        model_path=str(MODEL_DIR)
    )


@app.post("/tts", response_model=TTSResponse)
def synthesize(request: TTSRequest):
    if tts_engine is None:
        raise HTTPException(status_code=503, detail="TTS model not loaded")

    if not request.text:
        raise HTTPException(status_code=400, detail="Text cannot be empty")

    try:
        audio = tts_engine.generate(
            text=request.text,
            sid=request.sid,
            speed=request.speed,
        )

        if audio.samples is None or len(audio.samples) == 0:
            return TTSResponse(
                audio_base64="",
                sample_rate=0,
                success=False,
                error="Synthesis failed, no audio data"
            )

        wav_bytes = samples_to_wav_bytes(audio.samples, audio.sample_rate)
        import base64
        audio_base64 = base64.b64encode(wav_bytes).decode("utf-8")

        return TTSResponse(
            audio_base64=audio_base64,
            sample_rate=audio.sample_rate,
            success=True,
            error=None
        )

    except Exception as e:
        return TTSResponse(
            audio_base64="",
            sample_rate=0,
            success=False,
            error=f"Synthesis failed: {str(e)}"
        )