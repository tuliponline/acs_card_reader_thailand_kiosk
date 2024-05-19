import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:acs_card_reader_thailand/model/personnalInfomation_model.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';

class AcsCardReaderThailand {
  static const MethodChannel _channel =
      MethodChannel('acs_card_reader_thailand');

  static const EventChannel messageChannel = EventChannel('eventChannelStream');

  static Stream<bool> get messageStream async* {
    if (Platform.isAndroid) {
      await for (bool message in messageChannel
          .receiveBroadcastStream()
          .map((message) => message)) {
        yield message;
      }
    } else {
      yield false;
    }
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<PersonalInformationModel> acsCardID() async {
    return Platform.isAndroid
        ? thailandCard()
        : Future.value(PersonalInformationModel(
            address: "address",
            birthDate: "birthDate",
            cardIssuer: "cardIssuer",
            expireDate: "expireDate",
            gender: 1,
            issueDate: "issueDate",
            messageCode: 4,
            nameEn: "nameEn",
            nameTh: "nameTh",
            personalId: "personalId",
            picturePreFix: "picturePreFix",
            pictureSubFix: "pictureSubFix",
            status: false));
  }

  static Future<PersonalInformationModel> thailandCard() async {
    dynamic data = await (_channel.invokeMethod("acs_card"));
    Logger().d("data : $data");

    print("data : $data");

    Map<String, dynamic> json = jsonDecode(data);
    print(json);
    print("json counr : ${json.length}");
    json.forEach((key, value) {
      print("key : $key , value : $value");
    });

    PersonalInformationModel personalInformationModel =
        personalInformationModelFromJson(data);
    print(personalInformationModel.address);

    return personalInformationModel;
  }
}
