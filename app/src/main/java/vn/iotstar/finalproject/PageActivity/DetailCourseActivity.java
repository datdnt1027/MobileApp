package vn.iotstar.finalproject.PageActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iotstar.finalproject.Adapter.CartAdapter;
import vn.iotstar.finalproject.Adapter.FeedbackAdapter;
import vn.iotstar.finalproject.Adapter.NoticeAdapter;
import vn.iotstar.finalproject.Adapter.TypicalCourseAdapter;
import vn.iotstar.finalproject.Database.CartDatabase;
import vn.iotstar.finalproject.Model.Feedback;
import vn.iotstar.finalproject.Model.GiaoVien;
import vn.iotstar.finalproject.Model.KhoaHoc;
import vn.iotstar.finalproject.R;
import vn.iotstar.finalproject.Response.StatisticResponse;
import vn.iotstar.finalproject.Retrofit.GeneralAPI;
import vn.iotstar.finalproject.Retrofit.KhoaHocAPI;
import vn.iotstar.finalproject.Retrofit.RetrofitClient;
import vn.iotstar.finalproject.Storage.CartItem;
import vn.iotstar.finalproject.databinding.ActivityMain2Binding;
import vn.iotstar.finalproject.databinding.CourseInforLayoutBinding;

public class DetailCourseActivity extends AppCompatActivity {

    String courseId;
    GeneralAPI apiService;

    KhoaHocAPI apiService2;

    KhoaHoc khoaHoc;

    GiaoVien giaoVien;

    CourseInforLayoutBinding binding;

    CartItem new_item;

    FeedbackAdapter adapter;

    List<Feedback> list_feed;
    String maKhoaHoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CourseInforLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            courseId = extras.getString("courseId");
        }
        GetCourseInfor();
        GetTeacherInfor();
        if (MainActivity.role.equals("HV")) {
            binding.addToCartBtn.setVisibility(View.VISIBLE);
            addCartBtn();
        }
        else
            binding.addToCartBtn.setVisibility(View.INVISIBLE);
        getRating();
        HienBaiHoc();
    }

    private void GetCourseInfor() {
        apiService = RetrofitClient.getRetrofit().create(GeneralAPI.class);

        apiService.getCourseDetail(courseId).enqueue(new Callback<KhoaHoc>() {
            @Override
            public void onResponse(Call<KhoaHoc> call, Response<KhoaHoc> response) {
                if(response.isSuccessful()) {
                    khoaHoc = response.body();
                    maKhoaHoc=khoaHoc.getMaKhoaHoc();
                    binding.courseNamee.setText(khoaHoc.getTenKhoaHoc());
                    binding.courseDescript.setText(khoaHoc.getMoTa());
                    binding.courseCost.setText("Giá: "+khoaHoc.getGiaTien() +"đ");
                    binding.lessonNum.setText("Số bài học: "+ khoaHoc.getSoBaiHoc());
                }else {
                    int statusCode = response.code();
                }
            }

            @Override
            public void onFailure(Call<KhoaHoc> call, Throwable t) {
                Log.d("logg",t.getMessage());
            }
        });
    }
    private void GetTeacherInfor() {
        apiService = RetrofitClient.getRetrofit().create(GeneralAPI.class);

        apiService.getTeacherInfor(courseId).enqueue(new Callback<GiaoVien>() {
            @Override
            public void onResponse(Call<GiaoVien> call, Response<GiaoVien> response) {
                if(response.isSuccessful()) {
                    giaoVien = response.body();
                    binding.teacherName.setText(giaoVien.getTenGiaoVien());
                    binding.teacherSdt.setText(giaoVien.getSdt());
                    binding.teacherEmail.setText(giaoVien.getEmail());
//                    Toast.makeText(DetailCourseActivity.this, "Email: " + giaoVien.getEmail(), Toast.LENGTH_SHORT).show();
                }else {
                    int statusCode = response.code();
                }
            }
            @Override
            public void onFailure(Call<GiaoVien> call, Throwable t) {
                Log.d("logg",t.getMessage());
            }
        });
    }
    private void addCartBtn(){
        binding.addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCheckExist(khoaHoc)) {
                    new_item= new CartItem(khoaHoc);
                    CartDatabase.getInstance(DetailCourseActivity.this).cartDao().insertAll(new_item);
                    Toast.makeText(DetailCourseActivity.this, "Đã thêm vào giỏ thành công", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(DetailCourseActivity.this, "Khóa học đã tồn tại trong giỏ hàng của bạn", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("result","1");
//        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
    public  void HienBaiHoc()
    {
        binding.textView10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCourseLession(maKhoaHoc);


            }
        });
    }
    public void goToCourseLession(String maKhoaHoc)
    {
        Intent intent = new Intent(DetailCourseActivity.this, LessionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("maKhoaHoc", maKhoaHoc);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private boolean isCheckExist(@NonNull KhoaHoc kH){
        List<CartItem> list = CartDatabase.getInstance(this).cartDao().checkCourse(kH.getMaKhoaHoc(), MainActivity.userId);
        return list!=null && !list.isEmpty();
    }

    private void getRating()
    {
        apiService2 = RetrofitClient.getRetrofit().create(KhoaHocAPI.class);
        list_feed= new ArrayList<>();
        apiService2.getFeedback(courseId).enqueue(new Callback<List<Feedback>>() {
            @Override
            public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                if(response.isSuccessful()) {
                    list_feed = response.body();
                    adapter = new FeedbackAdapter(getApplicationContext(), list_feed);
                    binding.rcFeedback.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.rcFeedback.setLayoutManager(layoutManager);
                    binding.rcFeedback.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
//                    Toast.makeText(DetailCourseActivity.this, "Email: " + giaoVien.getEmail(), Toast.LENGTH_SHORT).show();
                }else {
                    int statusCode = response.code();
                }
            }
            @Override
            public void onFailure(Call<List<Feedback>> call, Throwable t) {
                Log.d("logg",t.getMessage());
            }
        });
    }
}