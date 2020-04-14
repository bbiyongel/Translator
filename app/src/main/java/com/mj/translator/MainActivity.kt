package com.mj.translator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

public class MainActivity : AppCompatActivity() {

    var sourceLang = ""
    var targetLang = ""
    var sourceLang2 = ""
    var targetLang2 = ""

    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        copybtn1.setOnClickListener {
            myClip = ClipData.newPlainText("text", papago.text);
            myClipboard?.setPrimaryClip(myClip);

            Toast.makeText(this, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show();
        }

        copybtn2.setOnClickListener {
            myClip = ClipData.newPlainText("text", kakao.text);
            myClipboard?.setPrimaryClip(myClip);

            Toast.makeText(this, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show();
        }

        sourcebtn.setOnClickListener {
            val popup = PopupMenu(this,sourcebtn)
            popup.inflate(R.menu.lang)
            popup.setOnMenuItemClickListener {
                sourcebtn.text = it.title
                true
            }
            popup.show()
        }

        targetbtn.setOnClickListener {
            val popup = PopupMenu(this,targetbtn)
            popup.inflate(R.menu.lang)
            popup.setOnMenuItemClickListener {
                targetbtn.text = it.title
                true
            }
            popup.show()
        }

        changebtn.setOnClickListener {
            val temp = sourcebtn.text

            sourcebtn.text = targetbtn.text
            targetbtn.text = temp
        }



        translatebtn.setOnClickListener {

            if(editText.text.toString().length == 0){
                Toast.makeText(this,"번역할 내용을 입력하세요",Toast.LENGTH_SHORT).show()
            }

            else if(sourcebtn.text == targetbtn.text){
                Toast.makeText(this,"번역 언어가 동일합니다. 다른 언어로 선택하세요",Toast.LENGTH_SHORT).show()
            }

            else{

                if (sourcebtn.text == "영어"){
                    sourceLang = "en"
                    sourceLang2 = "en"
                }
                else if (sourcebtn.text == "한국어"){
                    sourceLang = "ko"
                    sourceLang2 = "kr"
                }
                else if (sourcebtn.text == "일본어"){
                    sourceLang = "ja"
                    sourceLang2 = "jp"
                }
                else if (sourcebtn.text == "중국"){
                    sourceLang = "zh-CN"
                    sourceLang2 = "cn"
                }

                if (targetbtn.text == "영어"){
                    targetLang = "en"
                    targetLang2 = "en"
                }
                else if (targetbtn.text == "한국어"){
                    targetLang = "ko"
                    targetLang2 = "kr"
                }
                else if (targetbtn.text == "일본어"){
                    targetLang = "ja"
                    targetLang2 = "jp"
                }
                else if (targetbtn.text == "중국어"){
                    targetLang = "zh-CN"
                    targetLang2 = "cn"
                }



                val asyncTask = NaverTranslateTask()
                asyncTask.execute(editText.text.toString())


                val asyncTask2 = KakaoTranslateTask()
                asyncTask2.execute(editText.text.toString())
            }

        }
    }

    inner class NaverTranslateTask() : AsyncTask<String, Void, String>() {


        val clientId = "AwAUJEDlEenilEpdoQFZ"
        val clientSecret = "wrHp5MN5uu"

//        val sourceLang = "en"
//        val targetLang = "ko"


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {

            val sourceText = params[0]
            try {
                val text = URLEncoder.encode(sourceText, "UTF-8")
                val apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                val url = URL(apiURL);
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection;
                con.requestMethod = "POST";
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                val postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + text;
                con.doOutput = true;
                val wr = DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                val responseCode = con.responseCode;
                var br: BufferedReader
                if (responseCode == 200) { // 정상 호출
                    br = BufferedReader(InputStreamReader(con.getInputStream()))
                } else {  // 에러 발생
                    br = BufferedReader(InputStreamReader(con.getErrorStream()))
                }
                var inputLine: String? = null
                val response = StringBuffer();
                while ({ inputLine = br.readLine(); inputLine }() != null) {
                    response.append(inputLine)
                }
                br.close()
                //System.out.println(response.toString());
                return response.toString()


            } catch (e:Exception){
                //System.out.println(e);

                return ""
            }
        }




        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            Log.d("background result1", result.toString())

            val gson: Gson =  GsonBuilder().create();
            val parser = JsonParser();
            val rootObj: JsonElement = parser.parse(result.toString())
                //원하는 데이터 까지 찾아 들어간다.
                .asJsonObject.get("message")
                .asJsonObject.get("result")
            //안드로이드 객체에 담기
            val items: TranslatedItem = gson.fromJson(rootObj.toString(), TranslatedItem::class.java)
            //Log.d("result", items.getTranslatedText());
            //번역결과를 텍스트뷰에 넣는다.
            papago.text = items.translatedText






        }


    }

    inner class KakaoTranslateTask() : AsyncTask<String, Void, String>() {


        val clientId = "AwAUJEDlEenilEpdoQFZ"
        val clientSecret = "wrHp5MN5uu"
        val apikey = "ee05a8584607f80bc72156c4c7106750"

//        val sourceLang = "en"
//        val targetLang = "kr"


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {

            val sourceText = params[0]
            try {
                val text = URLEncoder.encode(sourceText, "UTF-8")
                val postParams = "src_lang=" + sourceLang2 + "&target_lang=" + targetLang2 + "&query=" + text;
                val apiURL = "https://kapi.kakao.com/v1/translation/translate?"+postParams;
                val url = URL(apiURL);
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection;
                val userCredentails = apikey
                val basicAuth = "KakaoAK " + userCredentails
                con.setRequestProperty("Authorization", basicAuth);
                con.requestMethod = "GET"
                con.setRequestProperty("Content-Type", "application/x-www-for-urlencoded")
                con.setRequestProperty("charset", "utf-8")
                con.useCaches = false
                con.doInput = true
                con.doOutput = true
                val responseCode = con.responseCode
                var br: BufferedReader
                if (responseCode == 200) { // 정상 호출
                    br = BufferedReader(InputStreamReader(con.getInputStream()))
                } else {  // 에러 발생
                    br = BufferedReader(InputStreamReader(con.getErrorStream()))
                }


                var inputLine: String? = null
                val response = StringBuffer();
                while ({ inputLine = br.readLine(); inputLine }() != null) {
                    response.append(inputLine)
                }
                br.close()
                //System.out.println(response.toString());
                return response.toString()


            } catch (e:Exception){
                //System.out.println(e);

                return ""
            }
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            Log.d("background result2", result.toString())

            val gson: Gson =  GsonBuilder().create();
            val parser = JsonParser();
            val rootObj: JsonElement = parser.parse(result.toString())
                .asJsonObject.get("translated_text")

            val translation = rootObj.asJsonArray.get(0).asJsonArray.get(0).toString().replace("\"","")

            kakao.text = translation






        }


    }

}
