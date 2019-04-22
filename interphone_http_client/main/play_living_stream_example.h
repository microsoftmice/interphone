/* ESP HTTP Client Example

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/

#ifndef _PLAY_LIVING_STREAM_EXAMPLE_H_
#define _PLAY_LIVING_STREAM_EXAMPLE_H_
#include "http_stream.h"

audio_event_iface_handle_t evt;

int void_http_stream_event_handle(http_stream_event_msg_t msg);
void stop_play_living_stream();
void star_play_living_stream();
void app();
void ptt(char * content);
int ptt_open;
int sq_open;
int wifi_ok;

#endif
