package com.hao.trade;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.leancloud.LCFile;
import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PublishActivity extends AppCompatActivity {

    private ImageView mImageViewSelect;
    private byte[] mImageBytes = null;
    private Handler mHandler = new Handler();
    private ProgressBar mProgerss;

    private String imageUrl;

//  private ProgressCallback mImageUploadProgressCallback = new ProgressCallback() {
//    @Override
//    public void done(Integer integer) {
//      final int mProgressStatus = integer;
//      mProgerss.setProgress(mProgressStatus);
//    }
//  };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        mImageViewSelect = (ImageView) findViewById(R.id.imageview_select_publish);
        mProgerss = (ProgressBar) findViewById(R.id.mProgess);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.publish));

        Button mButtonSelect = (Button) findViewById(R.id.button_select_publish);
        mButtonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 42);
            }
        });

        final EditText mDiscriptionEdit = (EditText) findViewById(R.id.edittext_discription_publish);
        final EditText mTitleEdit = (EditText) findViewById(R.id.edittext_title_publish);
        final EditText mPriceEdit = (EditText) findViewById(R.id.edittext_price_publish);

        findViewById(R.id.button_submit_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(mTitleEdit.getText().toString())) {
                    Toast.makeText(PublishActivity.this, "请输入标题", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("".equals(mDiscriptionEdit.getText().toString())) {
                    Toast.makeText(PublishActivity.this, "请输入商品描述", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("".equals(mPriceEdit.getText().toString())) {
                    Toast.makeText(PublishActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mImageBytes == null) {
                    Toast.makeText(PublishActivity.this, "请选择一张照片", Toast.LENGTH_SHORT).show();
                    return;
                }
                mProgerss.setVisibility(View.VISIBLE);
                LCObject product = new LCObject("Product");
                product.put("title", mTitleEdit.getText().toString());
                product.put("description", mDiscriptionEdit.getText().toString());
                product.put("price", Integer.parseInt(mPriceEdit.getText().toString()));
                product.put("owner", LCUser.getCurrentUser());
                product.put("phone", LCUser.getCurrentUser().getMobilePhoneNumber());
                product.put("imageUrl", imageUrl);
                product.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LCObject avObject) {
                        mProgerss.setVisibility(View.GONE);
                        PublishActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgerss.setVisibility(View.GONE);
                        Toast.makeText(PublishActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == RESULT_OK) {
            try {
                mImageViewSelect.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                mImageBytes = getBytes(getContentResolver().openInputStream(data.getData()));
                uploadPhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadPhoto(){
        mProgerss.setVisibility(View.VISIBLE);
        LCFile file = new LCFile("productPic", mImageBytes);
        file.saveInBackground().subscribe(new Observer<LCFile>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(LCFile file) {
                System.out.println("文件保存完成。URL：" + file.getUrl());
                imageUrl = file.getUrl();
                mProgerss.setVisibility(View.GONE);
            }
            public void onError(Throwable throwable) {
                // 保存失败，可能是文件无法被读取，或者上传过程中出现问题
                mProgerss.setVisibility(View.GONE);
            }
            public void onComplete() {}
        });
    }
}