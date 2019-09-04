package pl.simplyinc.simplyclime.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.login
import kotlinx.android.synthetic.main.activity_register.register
import pl.simplyinc.simplyclime.R
import android.view.View.OnFocusChangeListener
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.network.VolleySingleton
import java.math.BigInteger
import java.security.MessageDigest


class RegisterActivity : AppCompatActivity() {

    var checklogin = false
    var checkemail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        progressreg.visibility = View.GONE

        login.onFocusChangeListener = OnFocusChangeListener { _, hasFocus:Boolean->
                if (!hasFocus && login.text.trim().toString().length > 3) {
                    checkifexist(true, false)
                }
        }

        email.onFocusChangeListener = OnFocusChangeListener { _, hasFocus:Boolean->
            if (!hasFocus && email.text.trim().toString().length > 5) {
                checkifexist(false, false)
            }
        }
        register.setOnClickListener {
            regerror.text = ""
            if(checkData()){
                if(checklogin) {
                    if(checkemail)
                        registerRequest()
                    else {
                        checkifexist(false, true)
                        regerror.text = getString(R.string.mailexist)
                    }
                }else {
                    checkifexist(true, true)
                    regerror.text = getString(R.string.usernameexist)
                }
            }

        }
    }

    private fun checkData():Boolean{
        var ok = false
        val username = login.text.trim().toString()
        val emaill = email.text.trim().toString()
        val passwordd = pass.text.trim().toString()

        if(username.length < 4)
            login.error = getString(R.string.min4sign)
        else if(emaill.length < 6)
            email.error = getString(R.string.correctemail)
        else if(passwordd == username)
            pass.error = getString(R.string.passequalsuser)
        else if(passwordd.length < 5)
            pass.error = getString(R.string.minsignpassword)
        else ok = true

        return ok
    }

    private fun registerRequest(){
        progressreg.visibility = View.VISIBLE
        register.visibility = View.GONE

        val url = "http://$server/api/user/add"

        val request = object:StringRequest(Method.POST, url, Response.Listener{ res ->

            progressreg.visibility = View.GONE
            register.visibility = View.VISIBLE
            val response = JSONObject(res)

            if(!response.getBoolean("error")){
                val session = SessionPref(this)
                session.setPref("login", response.getString("login"))
                session.setPref("id", response.getString("id"))
                session.setPref("token", response.getString("token"))

                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()

            }else {
                regerror.text = response.getString("message")
            }

        },
            Response.ErrorListener {
                regerror.text = getString(R.string.error)
                progressreg.visibility = View.GONE
                register.visibility = View.VISIBLE
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                    params["login"] = login.text.trim().toString()
                    params["email"] = email.text.trim().toString()
                    params["password"] = pass.text.trim().toString().md5()
                return params
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun checkifexist(whichCheck:Boolean, registerAfter:Boolean){
        progressreg.visibility = View.VISIBLE
        register.visibility = View.GONE

        val url = "http://$server/api/user/add/check"

        val request = object:StringRequest(Method.POST, url, Response.Listener{ res ->

                progressreg.visibility = View.GONE
                register.visibility = View.VISIBLE
                val response = JSONObject(res)

                if(!response.getBoolean("error")){
                    if(response.getBoolean("existError")) {
                        regerror.text = if (whichCheck) getString(R.string.usernameexist) else getString(R.string.mailexist)

                        if(whichCheck)
                            checklogin = false
                        else checkemail = false
                    }else {
                        if(whichCheck) {
                            if(regerror.text == getString(R.string.usernameexist))
                                regerror.text = ""

                            checklogin = true
                        }else {
                            if(regerror.text == getString(R.string.mailexist))
                                regerror.text = ""

                            checkemail = true
                        }
                        if(registerAfter)
                            registerRequest()
                    }
                }else {
                    regerror.text = response.getString("message")
                }

            },
            Response.ErrorListener {
                regerror.text = getString(R.string.error)
                progressreg.visibility = View.GONE
                register.visibility = View.VISIBLE
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                if (whichCheck)
                    params["login"] = login.text.trim().toString()
                else
                    params["email"] = email.text.trim().toString()
                return params
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun String.md5():String{
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32,'0').toUpperCase()
    }
}
