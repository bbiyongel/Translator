package com.mj.translator

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        translatebtn.setOnClickListener {

            val asyncTask = NaverTranslateTask()
            asyncTask.execute(editText.text.toString())
        }
    }

    inner class NaverTranslateTask() : AsyncTask<String, Void, String>() {


        val clientId = "AwAUJEDlEenilEpdoQFZ"
        val clientSecret = "wrHp5MN5uu"

        val sourceLang = "en"
        val targetLang = "ko"


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {

            val sourceText = params[0]
            try {
                //String text = URLEncoder.encode("만나서 반갑습니다.", "UTF-8");
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

            Log.d("background result", result.toString())

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

}
