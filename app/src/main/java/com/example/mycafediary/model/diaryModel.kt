package com.example.mycafediary.model

data class diaryModel ( var UID : String? = null,
                        var img_src : String? = null,
                        var cafe_name : String? = null,
                        var visit_date : Long? = null,
                        var place_check1 : Boolean = false,
                        var place_check2: Boolean = false,
                        var place_check3: Boolean = false,
                        var place_check4: Int = 0,
                        var menu : String? = null,
                        var diary_detail : String? = null,
                        var location : String? = null)