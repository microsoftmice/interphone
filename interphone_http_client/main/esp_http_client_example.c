/* ESP HTTP Client Example

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/

#include <string.h>
#include <stdlib.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_system.h"
#include "nvs_flash.h"
#include "play_living_stream_example.h"
#include "driver/gpio.h"
#include "periph_button.h"
#include "esp_http_client.h"
#include "audio_event_iface.h"


#define MAX_HTTP_RECV_BUFFER 512
static const char *TAG = "HTTP_CLIENT";
char *data;
int data_len;
/* Root cert for howsmyssl.com, taken from howsmyssl_com_root_cert.pem

   The PEM file was extracted from the output of this command:
   openssl s_client -showcerts -connect www.howsmyssl.com:443 </dev/null

   The CA root cert is the last cert given in the chain of certs.

   To embed it in the app binary, the PEM file is named
   in the component.mk COMPONENT_EMBED_TXTFILES variable.
*/
extern const char howsmyssl_com_root_cert_pem_start[] asm("_binary_howsmyssl_com_root_cert_pem_start");
extern const char howsmyssl_com_root_cert_pem_end[]   asm("_binary_howsmyssl_com_root_cert_pem_end");

esp_err_t _http_event_handler(esp_http_client_event_t *http_evt)
{
    switch(http_evt->event_id) {
        case HTTP_EVENT_ERROR:
            ESP_LOGI(TAG, "HTTP_EVENT_ERROR");
            break;
        case HTTP_EVENT_ON_CONNECTED:
            ESP_LOGI(TAG, "HTTP_EVENT_ON_CONNECTED");
            break;
        case HTTP_EVENT_HEADER_SENT:
            ESP_LOGI(TAG, "HTTP_EVENT_HEADER_SENT");
            break;
        case HTTP_EVENT_ON_HEADER:
            ESP_LOGI(TAG, "HTTP_EVENT_ON_HEADER, key=%s, value=%s", http_evt->header_key, http_evt->header_value);
            break;
        case HTTP_EVENT_ON_DATA:
            ESP_LOGI(TAG, "HTTP_EVENT_ON_DATA, len=%d", http_evt->data_len);
            if (!esp_http_client_is_chunked_response(http_evt->client)) {
                // Write out data
                 printf("----------------------%.*s\n", http_evt->data_len, (char*)http_evt->data);
				 int len = http_evt->data_len;
				 if (len != 0)
				 {
					data_len = http_evt->data_len;
					data = http_evt->data;
					char content[data_len];
					strncpy(content,data,data_len);
					content[data_len]='\0';
					ptt(content);
				 }
            }
            break;
        case HTTP_EVENT_ON_FINISH:
            ESP_LOGI(TAG, "HTTP_EVENT_ON_FINISH");
            break;
        case HTTP_EVENT_DISCONNECTED:
            ESP_LOGI(TAG, "HTTP_EVENT_DISCONNECTED");
            break;
    }
    return ESP_OK;
}
esp_err_t err;
static void http_rest()
{
    esp_http_client_config_t config = {
        .url = "http://47.100.196.114:8080/interphone/IotServer",
        .event_handler = _http_event_handler,
    };
    esp_http_client_handle_t client = esp_http_client_init(&config);

    // POST
    const char *post_data = "id=100000";
    //esp_http_client_set_url(client, "http://47.100.196.114:8080/interphone/IotServer");
    esp_http_client_set_method(client, HTTP_METHOD_POST);
    esp_http_client_set_post_field(client, post_data, strlen(post_data));
    err = esp_http_client_perform(client);
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "HTTP POST Status = %d, content_length = %d",
                esp_http_client_get_status_code(client),
                esp_http_client_get_content_length(client));
    } else {
        ESP_LOGE(TAG, "HTTP POST request failed: %d", err);
    }
    esp_http_client_cleanup(client);
}


void iot_server_http()
{
    ESP_LOGI(TAG, "Connected to AP, begin http example");
	while(true){
		vTaskDelay(3000 / portTICK_RATE_MS);
		if (ptt_open == 0 && sq_open ==0 && wifi_ok == 1)
		{
			http_rest();
		}
	}
	ESP_LOGI(TAG, "Finish http example");
    vTaskDelete(NULL);
}

