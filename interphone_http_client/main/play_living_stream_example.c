/* Play M3U HTTP Living stream

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/

#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_log.h"
#include "esp_wifi.h"
#include "nvs_flash.h"
#include "sdkconfig.h"
#include "audio_element.h"
#include "audio_pipeline.h"
#include "audio_event_iface.h"
#include "audio_common.h"
#include "http_stream.h"
#include "i2s_stream.h"
#include "wav_decoder.h"
#include "esp_http_client.h"
#include "esp_http_client_example.h"
#include "esp_peripherals.h"
#include "periph_wifi.h"
#include "audio_hal.h"
#include "periph_button.h"
#include "driver/gpio.h"
#include "driver/uart.h"
#include "soc/uart_struct.h"

esp_err_t ret;
static const char *TAG = "HTTP_LIVINGSTREAM_EXAMPLE";
audio_pipeline_handle_t pipeline;
audio_pipeline_handle_t pipeline_ASR;

audio_element_handle_t http_stream_reader, i2s_stream_writer, wav_decoder;
audio_element_handle_t http_stream_writer, i2s_stream_reader;
audio_event_iface_handle_t evt;

static esp_periph_handle_t wifi_periph_handle;
static TaskHandle_t                 wifi_set_tsk_handle;


#define ASR_SERVER_URI "http://47.100.196.114:8080/interphone/AsrServer"
#define GPIO_OUTPUT_IO_1    12
#define GPIO_OUTPUT_PIN_SEL   (1ULL<<GPIO_OUTPUT_IO_1)

static const int RX_BUF_SIZE = 1024;
#define TXD_PIN (GPIO_NUM_4)
#define RXD_PIN (GPIO_NUM_15)
#define ECHO_TEST_RTS  (UART_PIN_NO_CHANGE)
#define ECHO_TEST_CTS  (UART_PIN_NO_CHANGE)

int uart_ok = 1; 
int ptt_open = 0;
int sq_open = 0;
int wifi_ok = 0;

int sendData_uart(const char* logName, const char* data)
{
    const int len = strlen(data);
    const int txBytes = uart_write_bytes(UART_NUM_1, data, len);
    ESP_LOGW(logName, "Wrote %d bytes", txBytes);
    return txBytes;
}


void init_uart() {
    const uart_config_t uart_config = {
        .baud_rate = 9600,
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE
    };
    uart_param_config(UART_NUM_1, &uart_config);
    uart_set_pin(UART_NUM_1, TXD_PIN, RXD_PIN, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    //write uart
    uart_driver_install(UART_NUM_1, RX_BUF_SIZE * 2, 0, 0, NULL, 0);
    sendData_uart(TAG, "AT+DMOSETGROUP=0,430.1375,430.1375,0,5,0,0\r\n");
	//sendData_uart(TAG, "AT+DMOCONNE\r\n");
	//read uart
    uint8_t* data = (uint8_t*) malloc(RX_BUF_SIZE+1);
    const int rxBytes = uart_read_bytes(UART_NUM_1, data, RX_BUF_SIZE, 1000 / portTICK_RATE_MS);
    if (rxBytes > 0) {
            data[rxBytes] = 0;
            ESP_LOGW(TAG, "Read %d bytes: '%s'", rxBytes, data);
            ESP_LOG_BUFFER_HEXDUMP(TAG, data, rxBytes, ESP_LOG_INFO);
			char* c = "+DMOSETGROUP:0\r\n";
			if (strcmp(c, (char*)data) == 0 )
			{
				uart_ok = 0;
			}
    }
    free(data);
	uart_driver_delete(UART_NUM_1);
}


esp_err_t _http_stream_event_handle(http_stream_event_msg_t *msg)
{
    esp_http_client_handle_t http = (esp_http_client_handle_t)msg->http_client;
    char len_buf[16];
    static int total_write = 0;

    if (msg->event_id == HTTP_STREAM_PRE_REQUEST) {
        // set header
        ESP_LOGI(TAG, "[ + ] HTTP client HTTP_STREAM_PRE_REQUEST, lenght=%d", msg->buffer_len);
        esp_http_client_set_header(http, "x-audio-sample-rates", "16000");
        esp_http_client_set_header(http, "x-audio-bits", "16");
        esp_http_client_set_header(http, "x-audio-channel", "1");
        total_write = 0;
        return ESP_OK;
    }

    if (msg->event_id == HTTP_STREAM_ON_REQUEST) {
        // write data
        int wlen = sprintf(len_buf, "%x\r\n", msg->buffer_len);
		if (esp_http_client_write(http, len_buf, wlen) <= 0) {
            return ESP_FAIL;
        }
		if (esp_http_client_write(http, msg->buffer, msg->buffer_len) <= 0) {
            return ESP_FAIL;
        }
		if (esp_http_client_write(http, "\r\n", 2) <= 0) {
            return ESP_FAIL;
        }
		total_write += msg->buffer_len;
        printf("\033[A\33[2K\rTotal bytes written: %d\n", total_write);
        return msg->buffer_len;
    }

    if (msg->event_id == HTTP_STREAM_POST_REQUEST) {
        ESP_LOGI(TAG, "[ + ] HTTP client HTTP_STREAM_POST_REQUEST, write end chunked marker");
		if (esp_http_client_write(http, "0\r\n\r\n", 5) <= 0) {
            return ESP_FAIL;
        }
		return ESP_OK;
    }

    if (msg->event_id == HTTP_STREAM_FINISH_REQUEST) {
        ESP_LOGI(TAG, "[ + ] HTTP client HTTP_STREAM_FINISH_REQUEST");
        char *buf = calloc(1, 64);
        assert(buf);
        int read_len = esp_http_client_read(http, buf, 64);
        if (read_len <= 0) {
            free(buf);
            return ESP_FAIL;
        }
        buf[read_len] = 0;
        ESP_LOGI(TAG, "Got HTTP Response = %s", (char *)buf);
        free(buf);
        return ESP_OK;
    }
    return ESP_OK;
}

void ptt(char * content){
	ESP_LOGI(TAG, "[ * ] ******************** PTT IS PREEED********!!!");
//	audio_pipeline_stop(pipeline);
//	audio_pipeline_wait_for_stop(pipeline);
	//audio_pipeline_unlink(pipeline);
	//audio_pipeline_remove_listener(pipeline);
	//audio_pipeline_link(pipeline, (const char *[]) {"http",  "mp3", "i2s"}, 3);
	audio_element_set_uri(http_stream_reader, content);
	http_stream_restart(http_stream_reader);
	//audio_pipeline_set_listener(pipeline, evt);
	//audio_pipeline_resume(pipeline);
	audio_pipeline_run(pipeline);

}
void star_play_living_stream()
{

    esp_log_level_set("*", ESP_LOG_INFO);
	esp_log_level_set(TAG, ESP_LOG_DEBUG);

    ESP_LOGI(TAG, "[ 1 ] Start audio codec chip");
    audio_hal_codec_config_t audio_hal_codec_cfg =  AUDIO_HAL_ES8388_DEFAULT();
    audio_hal_handle_t hal = audio_hal_init(&audio_hal_codec_cfg, BOARD);
    audio_hal_ctrl_codec(hal, AUDIO_HAL_CODEC_MODE_DECODE, AUDIO_HAL_CTRL_START);

    ESP_LOGI(TAG, "[2.0] Create audio pipeline for playback");
    audio_pipeline_cfg_t pipeline_cfg = DEFAULT_AUDIO_PIPELINE_CONFIG();
    pipeline = audio_pipeline_init(&pipeline_cfg);

    audio_pipeline_cfg_t pipeline_cfg2 = DEFAULT_AUDIO_PIPELINE_CONFIG();
    pipeline_ASR = audio_pipeline_init(&pipeline_cfg2);


    ESP_LOGI(TAG, "[2.1] Create http stream to read data");
    http_stream_cfg_t http_cfg = HTTP_STREAM_CFG_DEFAULT();
    //http_cfg.event_handle = void_http_stream_event_handle;
    http_cfg.type = AUDIO_STREAM_READER;
    //http_cfg.enable_playlist_parser = true;
    http_stream_reader = http_stream_init(&http_cfg);
    
	ESP_LOGI(TAG, "[2.1.1] Create http stream to post data to server");
    http_stream_cfg_t http_cfg2 = HTTP_STREAM_CFG_DEFAULT();
    http_cfg2.type = AUDIO_STREAM_WRITER;
    http_cfg2.event_handle = _http_stream_event_handle;
    http_stream_writer = http_stream_init(&http_cfg2);

    ESP_LOGI(TAG, "[2.2.1] Create i2s stream to read audio data from codec chip");
  	i2s_stream_cfg_t i2s_cfg = I2S_STREAM_CFG_DEFAULT();
    i2s_cfg.type = AUDIO_STREAM_WRITER;
    i2s_stream_writer = i2s_stream_init(&i2s_cfg);

    ESP_LOGI(TAG, "[2.2] Create i2s stream to write data to codec chip");
	i2s_stream_cfg_t i2s_cfg2 = I2S_STREAM_CFG_DEFAULT();
    i2s_cfg2.type = AUDIO_STREAM_READER;
    i2s_stream_reader = i2s_stream_init(&i2s_cfg2);
	
 

    ESP_LOGI(TAG, "[2.3] Create aac decoder to decode aac file");
    wav_decoder_cfg_t wav_cfg = DEFAULT_WAV_DECODER_CONFIG();
    wav_decoder = wav_decoder_init(&wav_cfg);

    ESP_LOGI(TAG, "[2.4] Register all elements to audio pipeline");
    audio_pipeline_register(pipeline, http_stream_reader, "http");
    audio_pipeline_register(pipeline, wav_decoder,        "wav");
    audio_pipeline_register(pipeline, i2s_stream_writer,  "i2s");
	//**
	audio_pipeline_register(pipeline_ASR, i2s_stream_reader, "i2s_2");
    audio_pipeline_register(pipeline_ASR, http_stream_writer, "http_2");
	
    ESP_LOGI(TAG, "[2.5] Link it together http_stream-->wav_decoder-->i2s_stream-->[codec_chip]");
    audio_pipeline_link(pipeline, (const char *[]) {"http",  "wav", "i2s"}, 3);
	//**
    audio_pipeline_link(pipeline_ASR, (const char *[]) {"i2s_2", "http_2"}, 2);

    ESP_LOGI(TAG, "[2.6] Setup uri (http as http_stream, aac as aac decoder, and default output is i2s)");
	//audio_element_set_uri(http_stream_reader, "http://dl.espressif.com/dl/audio/adf_music.mp3");
	audio_element_set_uri(http_stream_reader, "http://47.100.196.114:8080/interphone/sys-ok.wav");
	//**
	audio_element_set_uri(http_stream_writer, ASR_SERVER_URI);

    ESP_LOGI(TAG, "[ 3 ] Start and wait for Wi-Fi network");

    ESP_LOGI(TAG, "[ 4 ] Setup event listener");
    audio_event_iface_cfg_t evt_cfg = AUDIO_EVENT_IFACE_DEFAULT_CFG();
    evt = audio_event_iface_init(&evt_cfg);

    ESP_LOGI(TAG, "[4.1] Listening event from all elements of pipeline");
    audio_pipeline_set_listener(pipeline, evt);
	//**
	audio_pipeline_set_listener(pipeline_ASR, evt);
    ESP_LOGI(TAG, "[4.2] Listening event from peripherals");
    audio_event_iface_set_listener(esp_periph_get_event_iface(), evt);

    ESP_LOGI(TAG, "[ 5 ] Start audio_pipeline");
	//audio_pipeline_run(pipeline);
}
void Gpio_init(){
	ESP_LOGI(TAG, "[ 1.1 ] Initialize GPIO!");
	gpio_config_t io_conf;
    //disable interrupt
    io_conf.intr_type = GPIO_PIN_INTR_DISABLE;
    //set as output mode
    io_conf.mode = GPIO_MODE_OUTPUT;
    //bit mask of the pins that you want to set,e.g.GPIO18/19
    io_conf.pin_bit_mask = GPIO_OUTPUT_PIN_SEL;
    //disable pull-down mode
    io_conf.pull_down_en = 0;
    //disable pull-up mode
    io_conf.pull_up_en = 0;
    //configure GPIO with the given settings
    gpio_config(&io_conf);
	gpio_set_level(GPIO_OUTPUT_IO_1, 1);
}

void wifi_set_task (void *para)
{
    periph_wifi_config_start(wifi_periph_handle, WIFI_CONFIG_ESPTOUCH);
    if (ESP_OK == periph_wifi_config_wait_done(wifi_periph_handle, 30000 / portTICK_PERIOD_MS)) {
        ESP_LOGI(TAG, "Wi-Fi setting successfully");
    } else {
        ESP_LOGE(TAG, "Wi-Fi setting timeout");
    }
    wifi_set_tsk_handle = NULL;
    vTaskDelete(NULL);
}
void wifi_get_pawd(){
	nvs_handle my_handle;
    ret = nvs_open("storage", NVS_READWRITE, &my_handle);
    if (ret != ESP_OK) {
        ESP_LOGE(TAG, "Error (%d) opening NVS handle!\n", ret);
    } else {
        // Read
        ESP_LOGI(TAG, "Reading restart counter from NVS ... ");
		size_t required_size;
		nvs_get_str(my_handle, "server_name", NULL, &required_size);
		char* server_name = malloc(required_size);
        ret = nvs_get_str(my_handle, "server_name", server_name, &required_size);
		size_t required_size_ssid;
		nvs_get_str(my_handle, "ssid", NULL, &required_size_ssid);
		char* ssid = malloc(required_size_ssid);
        ret = nvs_get_str(my_handle, "ssid", ssid, &required_size_ssid);
        switch (ret) {
            case ESP_OK:
                ESP_LOGI(TAG, "ssid = %s",ssid);
                ESP_LOGI(TAG, "server_name = %s", server_name);
				periph_wifi_cfg_t wifi_cfg = {
					.ssid = ssid,
					.password = server_name,
				};
				wifi_periph_handle = periph_wifi_init(&wifi_cfg);
				esp_periph_start(wifi_periph_handle);
			    //×èÈûÔËÐÐ
				//periph_wifi_wait_for_connected(wifi_periph_handle, portMAX_DELAY);
                break;
            case ESP_ERR_NVS_NOT_FOUND:
                ESP_LOGE(TAG, "The value is not initialized yet!\n");
				periph_wifi_cfg_t wifi_cfg2 = {
					.ssid = CONFIG_WIFI_SSID,
					.password = CONFIG_WIFI_PASSWORD,
				};
				wifi_periph_handle = periph_wifi_init(&wifi_cfg2);
				esp_periph_start(wifi_periph_handle);
                break;
            default :
                ESP_LOGE(TAG, "Error (%d) reading!\n", ret);
        }
	}
	// Close
	nvs_close(my_handle);
}
void app_main(void){
    ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES) {
      ESP_ERROR_CHECK(nvs_flash_erase());
      ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
	tcpip_adapter_init();
	// gpio
	Gpio_init();
	//init_uart
	//do{
		//init_uart();
	//}while(uart_ok);
	ESP_LOGI(TAG, "[ 1 ] Initialize Button Peripheral & Connect to wifi network");
///////////////////////////////////////////////////////////////////////////////////////
    // Initialize peripherals management
    esp_periph_config_t periph_cfg = { 0 };
    esp_periph_init(&periph_cfg);
    // Initialize Button peripheral
	periph_button_cfg_t btn_cfg = {
        .gpio_mask = GPIO_SEL_5 | GPIO_SEL_18, //REC BTN & MODE BTN
    };
	esp_periph_handle_t button_handle = periph_button_init(&btn_cfg);
    // Start wifi & button peripheral
    esp_periph_start(button_handle);
	// nvs
	wifi_get_pawd(); 
	// http
	ESP_LOGI(TAG, "[ 1.2 ] Starting star_play_living_stream()");
	// http
	xTaskCreate(iot_server_http, "iot_server_http", 8192, NULL, 5, NULL);
	//
	star_play_living_stream();
    while (1) {
        audio_event_iface_msg_t msg;
        esp_err_t ret = audio_event_iface_listen(evt, &msg, portMAX_DELAY);
        if (ret != ESP_OK) {
            ESP_LOGE(TAG, "[ * ] Event interface error : %d", ret);
            continue;
        }
        if (msg.source_type == AUDIO_ELEMENT_TYPE_ELEMENT
                && msg.source == (void *) wav_decoder
                && msg.cmd == AEL_MSG_CMD_REPORT_MUSIC_INFO) {
            ESP_LOGW(TAG, "[ * ] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> /n");
            audio_element_info_t music_info = {0};
            audio_element_getinfo(wav_decoder, &music_info);
            ESP_LOGI(TAG, "[ * ] Receive music info from aac decoder, sample_rates=%d, bits=%d, ch=%d",
                     music_info.sample_rates, music_info.bits, music_info.channels);
            audio_element_setinfo(i2s_stream_writer, &music_info);
            i2s_stream_set_clk(i2s_stream_writer, music_info.sample_rates, music_info.bits, music_info.channels);
			ESP_LOGE(TAG, "....................Set PTT OPEN");
			ptt_open = 1;
			gpio_set_level(GPIO_OUTPUT_IO_1, 0);
            continue;
        }
		if (msg.source_type == AUDIO_ELEMENT_TYPE_ELEMENT && msg.source == (void *) i2s_stream_writer
            && msg.cmd == AEL_MSG_CMD_REPORT_STATUS && (int) msg.data == AEL_STATUS_STATE_STOPPED) {
			ESP_LOGE(TAG, "....................Set PTT CLOSE");
			ptt_open = 0;
			gpio_set_level(GPIO_OUTPUT_IO_1, 1);
			audio_pipeline_stop(pipeline);
			audio_pipeline_wait_for_stop(pipeline);
			audio_element_stop(wav_decoder);
			audio_element_wait_for_stop(wav_decoder);
			audio_element_stop(i2s_stream_writer);
			audio_element_wait_for_stop(i2s_stream_writer);
			audio_element_reset_output_ringbuf(wav_decoder);
			audio_element_reset_output_ringbuf(i2s_stream_writer);
			audio_element_reset_input_ringbuf(wav_decoder);
			audio_element_reset_input_ringbuf(i2s_stream_writer);
            continue;
        }
        /* restart stream when the first pipeline element (http_stream_reader in this case) receives stop event (caused by reading errors) */
        if (msg.source_type == AUDIO_ELEMENT_TYPE_ELEMENT && msg.source == (void *) http_stream_reader
                && msg.cmd == AEL_MSG_CMD_REPORT_STATUS && (int) msg.data == AEL_STATUS_STATE_STOPPED ) {
            ESP_LOGW(TAG, "[ * ] ################## AEL_STATUS_STATE_STOPPED????  ###################");
            continue;
        } 
		// Advance to the next song when previous finishes
		if (msg.source == (void *) i2s_stream_writer
			&& msg.cmd == AEL_MSG_CMD_REPORT_STATUS) {
			audio_element_state_t el_state = audio_element_get_state(i2s_stream_writer);
			if (el_state == AEL_STATE_FINISHED) {
				ESP_LOGW(TAG, "[ * ] i2s_stream_writer el_state is AEL_STATE_FINISHED,PTT is breaked!");
			}
			continue;
		}
		if ((int)msg.data == GPIO_NUM_5 && msg.cmd == PERIPH_BUTTON_LONG_PRESSED) {
            ESP_LOGW(TAG, "[ * ] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< %s",ASR_SERVER_URI);
			sq_open = 1;
 		    i2s_stream_set_clk(i2s_stream_reader, 16000, 16, 1);
			audio_pipeline_run(pipeline_ASR);
			continue;
        } 
		if ((int)msg.data == GPIO_NUM_5 && msg.cmd == PERIPH_BUTTON_LONG_RELEASE) {
            ESP_LOGW(TAG, "[ * ] ############################################");
			sq_open = 0;
			audio_pipeline_stop(pipeline_ASR);
            audio_pipeline_wait_for_stop(pipeline_ASR);
			continue;
        }
		if ((int)msg.data == GPIO_NUM_18 && msg.cmd == PERIPH_BUTTON_LONG_PRESSED) {
            ESP_LOGW(TAG, "[ * ] <<<<<<<<  SET WIFI >>>>>>>>>>");
			if (NULL == wifi_set_tsk_handle) {
				if (xTaskCreate(wifi_set_task, "WifiSetTask", 3 * 1024, NULL,
								5, &wifi_set_tsk_handle) != pdPASS) {
					ESP_LOGE(TAG, "Error create WifiSetTask");
				}
				//led_indicator_set(0, led_work_mode_setting);
			} else {
				ESP_LOGW(TAG, "WifiSetTask has already running");
			}
			continue;
        } 
		if (msg.source_type == PERIPH_ID_WIFI) {
			if (msg.cmd == PERIPH_WIFI_CONNECTED) {
				ESP_LOGI(TAG, "PERIPH_WIFI_CONNECTED data = %s",(char*)msg.data);
				//audio_element_set_uri(http_stream_reader, "http://dl.espressif.com/dl/audio/adf_music.mp3");
				audio_element_set_uri(http_stream_reader, "http://47.100.196.114:8080/interphone/sys-ok.wav");
				audio_pipeline_run(pipeline);
				wifi_ok = 1;
				//duer_que_send(duer_que, DUER_CMD_LOGIN, NULL, 0, 0, 0);
				//led_indicator_set(0, led_work_mode_connectok);
				// xEventGroupSetBits(duer_task_evts, WIFI_CONNECT_BIT);
			} else if (msg.cmd == PERIPH_WIFI_DISCONNECTED) {
				ESP_LOGI(TAG, "PERIPH_WIFI_DISCONNECTED ");
				wifi_ok = 0;
				//led_indicator_set(0, led_work_mode_disconnect);
			}
			continue;
		}
    }
}


void stop_play_living_stream(){

    ESP_LOGI(TAG, "[ 6 ] Stop audio_pipeline");
    audio_pipeline_terminate(pipeline);

    /* Terminate the pipeline before removing the listener */
    audio_pipeline_remove_listener(pipeline);

    /* Stop all peripherals before removing the listener */
    esp_periph_stop_all();
    audio_event_iface_remove_listener(esp_periph_get_event_iface(), evt);

    /* Make sure audio_pipeline_remove_listener & audio_event_iface_remove_listener are called before destroying event_iface */
    audio_event_iface_destroy(evt);

    /* Release all resources */
    audio_pipeline_deinit(pipeline);
    audio_element_deinit(http_stream_reader);
    audio_element_deinit(i2s_stream_writer);
    audio_element_deinit(wav_decoder);
    
	esp_periph_destroy();
}