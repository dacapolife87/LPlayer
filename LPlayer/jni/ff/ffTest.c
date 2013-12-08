#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "avcodec.h"
#include "avformat.h"
#include "swscale.h"
#include <com_N_ffmpeg_ForJNI.h>
#define AUDIO_INBUF_SIZE 20480
#define AUDIO_REFILL_THRESH 4096

JNIEXPORT jint JNICALL Java_capston_L_Player_ForJNI_open(JNIEnv *pEnv, jobject object, jstring inPath, jstring outPath){
	
    const char *in_filepath;
	const char *out_filepath;
	
    AVCodec *decoding_codec;
    AVCodec *encoding_codec;
    
    AVCodecContext *decoding_c= NULL;
    AVCodecContext *encoding_c = NULL;

    AVFormatContext *FormatCtx = NULL;
    AVPacket avpkt;

    int AudioStream = -1;
    int out_size, i, len = 0, e=0;
    FILE *outfile;
    uint8_t *outbuf;
    uint8_t *tmp_buf;

    in_filepath = (*pEnv)->GetStringUTFChars(pEnv, inPath, NULL);
    out_filepath = (*pEnv)->GetStringUTFChars(pEnv, outPath, NULL);

    av_register_all();    
    av_init_packet(&avpkt);

    /* 저장되어 있는 파일을 오픈 시켜 FormatCtx에 저장 */
    if(av_open_input_file(&FormatCtx, in_filepath, NULL, 0, NULL) != 0)
        return -1;
    
    if (av_find_stream_info(FormatCtx) < 0)
        return -2;
    
    for (i = 0; i < FormatCtx->nb_streams; i++) {
        if (FormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            AudioStream = i;            
        }
    }

    if (AudioStream == -1)
        return -3;

    decoding_c = FormatCtx->streams[AudioStream]->codec;
    decoding_codec = avcodec_find_decoder(decoding_c->codec_id);
    
    if(!decoding_codec)
        return -4;    

        
    /* 오디오 코덱 오픈 */
    if (avcodec_open(decoding_c, decoding_codec) < 0)
        return -5;    

    outbuf = malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE);      //디코딩된 데이터 저장
    tmp_buf = malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE);     //인코딩된 데이터 저장

    outfile = fopen(out_filepath, "wb");
    if (!outfile) {
        av_free(decoding_c);        
        return -6;
    }

    /* make encoding codec */
     /* find the MP2 encoder */
    encoding_codec = avcodec_find_encoder(CODEC_ID_MP2);
    if (!encoding_codec)
        return 1;   

    encoding_c= avcodec_alloc_context();

    /* put sample parameters */
    encoding_c->bit_rate = 64000;
    encoding_c->sample_rate = 44100;
    encoding_c->channels = 2;
    encoding_c->sample_fmt = AV_SAMPLE_FMT_S16;

    /* open it */
    if (avcodec_open(encoding_c, encoding_codec) < 0)
        return 2;


    /* decode until eof */  
    while (av_read_frame(FormatCtx, &avpkt) >= 0) {        
        if(avpkt.stream_index == AudioStream){
            out_size = AVCODEC_MAX_AUDIO_FRAME_SIZE;

            len = avcodec_decode_audio3(decoding_c, (short *)outbuf, &out_size, &avpkt);
            
            if (len < 0) {                
                return -7;
            }
            if (out_size > 0) {
                /* if a frame has been decoded, encoding it */
                e = avcodec_encode_audio(encoding_c, tmp_buf, out_size, (short *)outbuf);

                fwrite(tmp_buf, 1, e, outfile);
            }
            avpkt.size -= len;
            avpkt.data += len;
        }
    }

    fclose(outfile);    
    free(outbuf);

    avcodec_close(decoding_c);
    av_free(decoding_c);

	(*pEnv)->ReleaseStringUTFChars(pEnv, inPath, in_filepath);
	(*pEnv)->ReleaseStringUTFChars(pEnv, outPath, out_filepath);
	return len;
}

JNIEXPORT jint JNICALL Java_capston_L_Player_ForJNI_Init(JNIEnv *pEnv, jobject object){
	return 777;
}