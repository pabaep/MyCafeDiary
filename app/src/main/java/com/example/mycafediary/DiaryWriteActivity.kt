package com.example.mycafediary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_diary_write.*
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.widget.DatePicker
import com.bumptech.glide.Glide
import com.example.mycafediary.model.diaryModel
import com.google.firebase.database.FirebaseDatabase

class DiaryWriteActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    var firestore :FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage : FirebaseStorage? = null
    var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    var imgFileName = "IMG_" + timeStamp + "_.jpg"
    var selectedPhotoUri: Uri? = null
    var cal = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            }
        }
        back_btn.setOnClickListener {
            var intent = Intent(this, DiaryListActivity::class.java)
            startActivity(intent)
            finish()
        }
        cal_view.setOnClickListener {
            //TODO 달력 뷰 보여주기
            DatePickerDialog(
                    this,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)

            ).apply{}.show()

            val calString = cal.get(Calendar.YEAR).toString()+"년 "+(cal.get(Calendar.MONTH)+1).toString()+"월 "+cal.get(Calendar.DAY_OF_MONTH).toString()+"일"
            cal_view.text = calString

        }
        img_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK) //photo selector intent만들기
            intent.type = "image/*"  //우리가 원하는 intent type
            startActivityForResult(intent,0) //비트맵을 이용해서 이미지 로딩하는 function.
        }
        save_btn.setOnClickListener {
            var diaryModel = diaryModel()
            diaryModel.UID = auth?.currentUser?.uid
            diaryModel.cafe_name = cafename_txt.text.toString()
            diaryModel.visit_date = cal.timeInMillis
            diaryModel.place_check1 = plc1_check.isChecked
            diaryModel.place_check2 = plc2_check.isChecked
            diaryModel.place_check3 = plc3_check.isChecked
            diaryModel.place_check4 = Integer.parseInt(plc4_txt.text.toString())
            diaryModel.menu = menu_txt.text.toString()
            diaryModel.diary_detail = diary_detail_txt.text.toString()
            diaryModel.location = location_txt.text.toString()

            firestore?.collection("Records")?.document("record_${auth?.currentUser?.uid}_${cal.timeInMillis}")?.set(diaryModel)

            uploadImageToFirebaseStorage()//사진 올리는 코드
            var intent = Intent(this, DiaryListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //프로필 사진으로 선택한 이미지 보이게 하는 과정
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            //proceed and check what the selected image was ...
            //선택한 이미지가 보이게 하는 과정
            selectedPhotoUri = data.data //uri는 그 이미지가 저장된 location을 나타냄.
            //bitmap으로 우리가 선택한 이미지에 access하기.
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            if(bitmap != null){
                img_btn.setImageBitmap(bitmap)
                //img_btn.alpha = 0f
            }else{
                val icon = BitmapFactory.decodeResource(getResources(), R.drawable.photo_default)
                img_btn.setImageBitmap(icon)
                //img_btn.alpha = 0f
            }

        }
    }
    private fun uploadImageToFirebaseStorage() {

        //getInctance()를 이용해서 FirebaseStorage에 접근.
        //ref는 파이어베이스 upload area에 대한 정보.
        val ref = FirebaseStorage.getInstance().getReference("/images/$imgFileName")

        if(selectedPhotoUri == null){
            val uri = Uri.parse("android.resource://com.example.mycafediary/drawable/photo_default")
            // val stream: InputStream? = contentResolver.openInputStream(uri)
            ref.putFile(uri)//selected Photo된거 uri를 file형태로 ref에 넣음.
                    //이미지 업로드
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            firestore?.collection("Records")?.document("record_${auth?.currentUser?.uid}_${cal.timeInMillis}")?.update("img_src",imgFileName)

                        }
                    }
        }else{
            ref.putFile(selectedPhotoUri!!) //selected Photo된거 uri를 file형태로 ref에 넣음.
                    //이미지 업로드
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d("RegisterActivity","File Location: $it")
                            firestore?.collection("Records")?.document("record_${auth?.currentUser?.uid}_${cal.timeInMillis}")?.update("img_src",imgFileName)
                        }
                    }
                    .addOnFailureListener{ //실패하면
                    }
        }
    }


}