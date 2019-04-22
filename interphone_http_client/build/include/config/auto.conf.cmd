deps_config := \
	/home/tandan/esp/esp-adf/esp-idf/components/app_trace/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/aws_iot/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/bt/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/esp32/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/esp_adc_cal/Kconfig \
	/home/tandan/esp/esp-adf/components/esp_http_client/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/ethernet/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/fatfs/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/freertos/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/heap/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/libsodium/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/log/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/lwip/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/mbedtls/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/openssl/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/pthread/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/spi_flash/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/spiffs/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/tcpip_adapter/Kconfig \
	/home/tandan/esp/esp-adf/esp-idf/components/wear_levelling/Kconfig \
	/home/tandan/esp/esp-adf/components/audio_hal/Kconfig.projbuild \
	/home/tandan/esp/esp-adf/esp-idf/components/bootloader/Kconfig.projbuild \
	/home/tandan/esp/esp-adf/esp-idf/components/esptool_py/Kconfig.projbuild \
	/home/tandan/esp/esp-adf/ai-examples/player/interphone_http_client/main/Kconfig.projbuild \
	/home/tandan/esp/esp-adf/esp-idf/components/partition_table/Kconfig.projbuild \
	/home/tandan/esp/esp-adf/esp-idf/Kconfig

include/config/auto.conf: \
	$(deps_config)


$(deps_config): ;
