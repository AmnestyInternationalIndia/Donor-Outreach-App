package in.org.amnesty.outreach.fragments;


import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.pdfview.PDFView;

import java.io.File;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.helpers.Utils;

public class PdfViewerFragment extends BaseFragment {

    public static final String PDF_FILE_NAME= "pdfFileName";

    private PDFView mPdfView;

	public static PdfViewerFragment newInstance(Bundle arguments) {
		PdfViewerFragment fragment = new PdfViewerFragment();
        fragment.setArguments(arguments);
		return fragment;
	}

	public PdfViewerFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState) {
		View pdfView =  inflater.inflate (R.layout.fragment_pdf_view, container, false);
        mPdfView = (PDFView) pdfView.findViewById(R.id.pdfView);

        Bundle arguments = getArguments();

        if(arguments != null) {
            String pdfFileName  = arguments.getString(PDF_FILE_NAME);
            String pdfFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + pdfFileName;
            mPdfView.fromFile(new File(pdfFilePath));
        }
        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_PDF_VIEWER_FRAGMENT);
        return pdfView;
	}
}
