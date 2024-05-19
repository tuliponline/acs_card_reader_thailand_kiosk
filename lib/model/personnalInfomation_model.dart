// To parse this JSON data, do
//
//     final personalInformationModel = personalInformationModelFromJson(jsonString);

import 'dart:convert';

PersonalInformationModel personalInformationModelFromJson(String str) =>
    PersonalInformationModel.fromJson(json.decode(str));

String personalInformationModelToJson(PersonalInformationModel data) =>
    json.encode(data.toJson());

class PersonalInformationModel {
  String address;
  String birthDate;
  String cardIssuer;
  String expireDate;
  int gender;
  String issueDate;
  int messageCode;
  String nameEn;
  String nameTh;
  String personalId;
  String picturePreFix;
  String pictureSubFix;
  bool status;

  PersonalInformationModel({
    required this.address,
    required this.birthDate,
    required this.cardIssuer,
    required this.expireDate,
    required this.gender,
    required this.issueDate,
    required this.messageCode,
    required this.nameEn,
    required this.nameTh,
    required this.personalId,
    required this.picturePreFix,
    required this.pictureSubFix,
    required this.status,
  });

  factory PersonalInformationModel.fromJson(Map<String, dynamic> json) {
    print("json[i] : ${json["i"]}");
    print("json[h] : ${json["h"]}");
    return PersonalInformationModel(
      address: json["Address"] ?? json["e"] ?? "Address",
      birthDate: json["BirthDate"] ?? json["d"] ?? "BirthDate",
      cardIssuer: json["CardIssuer"] ?? json["i"] ?? "CardIssuer",
      expireDate: json["ExpireDate"] ?? json["d"] ?? "ExpireDate",
      gender: json["Gender"] ?? json["h"] ?? 1,
      issueDate: json["IssueDate"] ?? json["j"] ?? "IssueDate",
      messageCode: json["Message_code"] ?? json["m"] ?? 4,
      nameEn: json["NameEN"] ?? json["c"] ?? "NameEN",
      nameTh: json["NameTH"] ?? json["b"] ?? "NameTH",
      personalId: json["PersonalID"] ?? json["a"] ?? "PersonalID",
      picturePreFix: json["PicturePreFix"] ?? json["f"] ?? "PicturePreFix",
      pictureSubFix: json["PictureSubFix"] ?? json["g"] ?? "PictureSubFix",
      status: json["Status"] ?? json["l"] ?? false,
    );
  }

  Map<String, dynamic> toJson() => {
        "Address": address,
        "BirthDate": birthDate,
        "CardIssuer": cardIssuer,
        "ExpireDate": expireDate,
        "Gender": gender,
        "IssueDate": issueDate,
        "Message_code": messageCode,
        "NameEN": nameEn,
        "NameTH": nameTh,
        "PersonalID": personalId,
        "PicturePreFix": picturePreFix,
        "PictureSubFix": pictureSubFix,
        "Status": status,
      };
}
