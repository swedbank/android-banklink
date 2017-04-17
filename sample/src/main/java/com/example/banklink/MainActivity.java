package com.example.banklink;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.banklink.network.SignService;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.swedbank.sdk.banklink.BanklinkClient;
import com.swedbank.sdk.banklink.BanklinkResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

import static butterknife.ButterKnife.findById;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.subscriptions.Subscriptions.unsubscribed;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String PACKET_ID_KEY = "VK_SERVICE";
  private static final String DATETIME_KEY = "VK_DATETIME";
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
  private static final int BANKLINK_REQUEST_CODE = 0xA52;

  @BindView(R.id.main_container)
  ViewGroup mainContainer;
  @BindView(R.id.loading_container)
  ViewGroup loadingContainer;
  @BindView(R.id.result_container)
  ViewGroup resultContainer;
  @BindView(R.id.main_action)
  View mainActionBtn;
  @BindView(R.id.packet_selection)
  Spinner packetSelection;
  @BindView(R.id.packet_form_container)
  ViewGroup formContainer;
  @BindView(R.id.loading_text)
  TextView loadingText;
  @BindView(R.id.result_text)
  TextView resultText;
  @BindView(R.id.result_extra_msg)
  TextView resultExtraMsg;
  @BindView(R.id.result_icon)
  ImageView resultIcon;

  @Inject
  SignService signService;
  @Inject
  InputMethodManager imm;
  @Inject
  BanklinkClient banklinkClient;

  private RxActivityLifecycles lifecycles;
  @NonNull
  private Subscription mainActionSubscription = unsubscribed();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((SampleApp) getApplication()).appComponent().inject(this);

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    final Toolbar toolbar = findById(this, R.id.toolbar);
    setSupportActionBar(toolbar);
    resultExtraMsg.setMovementMethod(new ScrollingMovementMethod());
    lifecycles = RxActivityLifecycles.create(this);

    if (mainActionSubscription.isUnsubscribed()) {
      wireMainAction();
    }

    setupPackets();
  }

  private void setupPackets() {
    final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Packet.asList());
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    packetSelection.setAdapter(adapter);
    lifecycles.addSubscriptionForLife(RxAdapterView
        .itemSelections(packetSelection)
        .distinctUntilChanged()
        .map(Packet::fromSelection)
        .subscribe(this::changePacketSelection, Throwable::printStackTrace));
  }

  private void wireMainAction() {
    mainActionSubscription = RxView
        .clicks(mainActionBtn)
        .take(1)
        .doOnNext(__ -> {
          mainActionBtn.setVisibility(View.GONE);
          hideInput();
          showLoading(true, getString(R.string.progress_signing));
        })
        .map(__ -> gatherFormData())
        .flatMap(signService::sign)
        .observeOn(mainThread())
        .subscribe(this::sendBanklink, this::handleSigningErrors);
    lifecycles.addSubscriptionForLife(mainActionSubscription);
  }

  private void sendBanklink(@NonNull Map<String, String> signedPacketMap) {
    Log.d(TAG, "Signing success. Response:\n" + signedPacketMap);
    try {
      final Intent intent = banklinkClient.createBanklinkIntent(signedPacketMap);
      startActivityForResult(intent, BANKLINK_REQUEST_CODE);
      showLoading(true, getString(R.string.progress_send_to_payment));
    } catch (ActivityNotFoundException e) {
      showLoading(false, null);
      wireMainAction();
      banklinkClient.openSwedbankAppPlayStoreListing();
    }
  }

  private void handleSigningErrors(@NonNull Throwable e) {
    showLoading(false, null);
    wireMainAction();
    e.printStackTrace();
    Snackbar.make(mainContainer, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG)
        .show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BANKLINK_REQUEST_CODE) {
      final BanklinkResult result = banklinkClient.parseResult(resultCode, data);
      String extraMsg = result.responsePacket().toString();
      if (result.canceled()) {
        extraMsg = getString(R.string.result_cancel, extraMsg);
      }
      animateResult(result.success(), extraMsg);
      wireMainAction();
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void changePacketSelection(@NonNull Packet packet) {
    final LayoutInflater inflater = getLayoutInflater();
    final ViewGroup formContainer = this.formContainer;
    TransitionManager.beginDelayedTransition(formContainer, new AutoTransition().setDuration(150));
    formContainer.removeAllViews();
    final PacketParameter[] parameters = packet.parameters;
    final int paramLen = parameters.length;
    for (int i = 0; i < paramLen; i++) {
      final PacketParameter param = parameters[i];
      final TextInputLayout inputLayout = (TextInputLayout) inflater.inflate(R.layout.input_field, formContainer, false);
      inputLayout.setTag(param);
      inputLayout.setHint(param.id());
      final EditText inputField = inputLayout.getEditText();
      if (inputField != null) {
        inputField.setText(param.value());
      }
      formContainer.addView(inputLayout);
    }
  }

  private Map<String, String> gatherFormData() {
    final ViewGroup formContainer = this.formContainer;
    final int childCount = formContainer.getChildCount();
    final ArrayMap<String, String> formData = new ArrayMap<>(childCount);
    for (int i = 0; i < childCount; i++) {
      final View child = formContainer.getChildAt(i);
      final PacketParameter param = (PacketParameter) child.getTag();
      if (child instanceof TextInputLayout) {
        final EditText input = ((TextInputLayout) child).getEditText();
        if (input != null) {
          formData.put(param.id(), input.getText().toString());
        }
      }
    }
    final Packet selectedPacket = Packet.fromSelection(packetSelection.getSelectedItemPosition());
    formData.put(PACKET_ID_KEY, Integer.toString(selectedPacket.packetId));
    formData.put(DATETIME_KEY, DATE_FORMAT.format(new Date()));
    return formData;
  }

  private void hideInput() {
    final View currentFocus = getCurrentFocus();
    if (currentFocus != null) {
      imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
  }

  private void showLoading(boolean visible, @Nullable String message) {
    if (visible) {
      mainContainer.setVisibility(View.GONE);
      loadingContainer.setVisibility(View.VISIBLE);
      loadingText.setText(message);
    } else {
      mainActionBtn.setVisibility(View.VISIBLE);
      mainContainer.setVisibility(View.VISIBLE);
      loadingContainer.setVisibility(View.GONE);
      loadingText.setText(null);
    }
  }

  private void animateResult(boolean success, @Nullable String extraMsg) {
    mainContainer.setVisibility(View.GONE);
    loadingContainer.setVisibility(View.GONE);
    resultContainer.setVisibility(View.VISIBLE);

    resultText.setText(success ? R.string.result_success : R.string.result_fail);
    resultExtraMsg.setText(extraMsg);

    resultContainer.setAlpha(0f);
    resultContainer.animate()
        .setDuration(100)
        .alpha(1f);

    resultIcon.setImageResource(success ? R.drawable.ic_check : R.drawable.ic_clear);
    resultIcon.setScaleX(0f);
    resultIcon.setScaleY(0f);
    resultIcon.animate()
        .scaleX(1f)
        .scaleY(1f)
        .setInterpolator(new OvershootInterpolator());
  }

  @OnClick(R.id.result_next_btn)
  void onResultOkClick() {
    resultContainer.animate()
        .alpha(0f)
        .withEndAction(() -> {
          resultContainer.setVisibility(View.GONE);
          resultContainer.setAlpha(1f);
        });
    showLoading(false, null);
  }
}
