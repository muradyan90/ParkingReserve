package com.example.parkingreserve;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utils {


    public static final String carModelList[]={"Автокам",
            "Бронто",
            "ГАЗ",
            "Ё-мобиль",
            "ЗАЗ",
            "ЗИЛ",
            "ЗиС",
            "ИЖ",
            "Канонир",
            "Комбат",
            "ЛуАЗ",
            "Москвич",
            "СМ",
            "ТагАЗ",
            "УАЗ",
            "Эксклюзив",
            "AC",
            "Acura",
            "Adler",
            "Alfa Romeo",
            "Alpina",
            "Alpine",
            "AM General",
            "AMC",
            "Ariel",
            "Aro",
            "Asia",
            "Aston Martin",
            "Audi",
            "Austin",
            "Autobianchi",
            "Bajaj",
            "Baltijas Dzips",
            "Batmobile",
            "Beijing",
            "Bentley",
            "Bertone",
            "Bilenkin",
            "Bitter",
            "BMW",
            "Borgward",
            "Brabus",
            "Bricklin",
            "Brilliance",
            "Bristol",
            "Bufori",
            "Bugatti",
            "Buick",
            "BYD",
            "Byvin",
            "Cadillac",
            "Callaway",
            "Carbodies",
            "Caterham",
            "Changan",
            "ChangFeng",
            "Chery",
            "Chevrolet",
            "Chrysler",
            "Citroen",
            "Cizeta",
            "Coggiola",
            "Dacia",
            "Dadi",
            "Daewoo2",
            "Daihatsu",
            "Daimler",
            "Datsun",
            "De Tomaso",
            "Delage",
            "DeLorean",
            "Derways",
            "DeSoto",
            "Dodge",
            "DongFeng",
            "Doninvest",
            "Donkervoort",
            "DS",
            "E-Car",
            "Eagle",
            "Eagle Cars",
            "Ecomotors",
            "Excalibur",
            "FAW",
            "Ferrari",
            "Fiat",
            "Fisker",
            "Ford",
            "Foton",
            "FSO",
            "Fuqi",
            "Geely",
            "Genesis",
            "Geo",
            "GMC",
            "Gonow",
            "Gordon",
            "Great Wall",
            "Hafei",
            "Haima",
            "Hanomag",
            "Haval",
            "Hawtai",
            "Hindustan",
            "Hispano-Suiza",
            "Holden",
            "Honda",
            "HuangHai",
            "Hudson",
            "Hummer",
            "Hyundai",
            "Infiniti",
            "Innocenti",
            "Invicta",
            "Iran Khodro",
            "Isdera",
            "Isuzu",
            "JAC",
            "Jaguar",
            "Jeep",
            "Jensen",
            "JMC",
            "Kia",
            "Koenigsegg",
            "KTM AG",
            "LADA (ВАЗ)",
            "Lamborghini",
            "Lancia",
            "Land Rover",
            "Landwind",
            "Lexus",
            "Liebao Motor",
            "Lifan",
            "Lincoln",
            "Lotus",
            "LTI",
            "Luxgen",
            "Mahindra",
            "Marcos",
            "Marlin",
            "Marussia",
            "Maruti",
            "Maserati",
            "Maybach",
            "Mazda",
            "McLare1",
            "Mega",
            "Mercedes-Benz",
            "Mercury",
            "Metrocab",
            "MG",
            "Microcar",
            "Minelli",
            "MINI",
            "Mitsubishi",
            "Mitsuoka",
            "Morgan",
            "Morris",
            "Nissan",
            "Noble",
            "Oldsmobile",
            "Opel",
            "Osca",
            "Packard",
            "Pagani",
            "Panoz",
            "Perodua",
            "Peugeot",
            "PGO",
            "Piaggio",
            "Plymouth",
            "Pontiac",
            "Porsche",
            "Premier",
            "Proton",
            "PUCH",
            "Puma",
            "Qoros",
            "Qvale",
            "Ravon",
            "Reliant",
            "Renaissance",
            "Renault",
            "Renault Samsung",
            "Rezvani",
            "Rimac",
            "Rolls-Royce",
            "Ronart",
            "Rover",
            "Saab",
            "Saleen",
            "Santana",
            "Saturn",
            "Scion",
            "SEAT",
            "Shanghai Maple",
            "ShuangHuan",
            "Skoda",
            "Smart",
            "Soueast",
            "Spectre",
            "Spyker",
            "SsangYong",
            "Steyr",
            "Subaru",
            "Suzuki",
            "Talbot",
            "TATA",
            "Tatra",
            "Tazzari",
            "Tesla",
            "Think",
            "Tianma",
            "Tianye",
            "Tofas",
            "Toyota",
            "Trabant",
            "Tramontana",
            "Triumph",
            "TVR",
            "Ultima",
            "Vauxhall",
            "Vector",
            "Venturi",
            "Volkswagen",
            "Volvo",
            "Vortex",
            "W Motors",
            "Wanderer",
            "Wartburg",
            "Westfield",
            "Wiesmann",
            "Willys",
            "Xin Kai",
            "Zastava",
            "Zenos",
            "Zenvo",
            "Zibar",
            "Zotye",
            "ZX"};


    public static final HashMap<Integer,String> getCarColorList(Context context){


        HashMap <Integer,String> carColorlist=new HashMap<>();



        carColorlist.put(context.getResources().getColor(R.color.Black),"Black");
        carColorlist.put(context.getResources().getColor(R.color.Champagne),"Champagne");
        carColorlist.put(context.getResources().getColor(R.color.White),"White");
        carColorlist.put(context.getResources().getColor(R.color.Blue),"Blue");
        carColorlist.put(context.getResources().getColor(R.color.DarkBlue),"Dark Blue");
        carColorlist.put(context.getResources().getColor(R.color.Violet),"Violet");
        carColorlist.put(context.getResources().getColor(R.color.Yellow),"Yellow");
        carColorlist.put(context.getResources().getColor(R.color.Green),"Green");
        carColorlist.put(context.getResources().getColor(R.color.Grey),"Grey");
        carColorlist.put(context.getResources().getColor(R.color.Pink),"Pink");

        return carColorlist;
    }





    public static final String CAR_MODEL_DIALOG_TEXT ="Select Your Car Model";

    public static final String CAR_COLOR_DIALOG_TEXT="Select Your Car Color";



    public static final String AM_PHONE_CODE = "+374";
    public static final String PHONE_INTENT_KEY="phone number";
    public static final String PLATE_INTENT_KEY="plate number";
    public static final String COLOR_INTENT_KEY="car color";
    public static final String MODEL_INTENT_KEY="car model";














}
