/*
 * Copyright 2015 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package speakman.whatsshakingnz.ui.viewmodel;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.databinding.RowEarthquakeBindingImpl;
import speakman.whatsshakingnz.databinding.RowEarthquakeBindingLandImpl;
import speakman.whatsshakingnz.model.Earthquake;
import timber.log.Timber;

/**
 * Created by Adam on 11/29/2015.
 */
@SuppressWarnings("unused")
public class EarthquakeListViewModel extends EarthquakeOverviewViewModel {

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public interface OnClickListener {
            void onEarthquakeClick(View v, Earthquake earthquake);
        }

        public RowEarthquakeBinding binding;
        private OnClickListener clickListener;
        public ViewHolder(RowEarthquakeBinding binding, OnClickListener clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
            this.itemView.setOnClickListener(this);
        }

        public void onClick(View v) {
            /*
            http://developer.android.com/tools/data-binding/guide.html
            "When there are different layout files for various configurations (e.g. landscape or
            portrait), the variables will be combined. There must not be conflicting variable
            definitions between these layout files."
            Turns out the DataBinding library (at least the version included in 23.1.1 support libs)
            forgets to generate a getter for the models in the binding abstract base class. It
            generates two impl files - one for each resource variant I've defined - but only the
            setter is accessible from the base class. For that reason, we figure out what type it
            is and then cast it appropriately so we can get access to the method.
             */
            Earthquake earthquake = null;
            if (this.binding instanceof RowEarthquakeBindingImpl) {
                earthquake = ((RowEarthquakeBindingImpl) this.binding).getEarthquakeModel().getEarthquake();
            } else if (this.binding instanceof RowEarthquakeBindingLandImpl) {
                earthquake = ((RowEarthquakeBindingLandImpl) this.binding).getEarthquakeModel().getEarthquake();
            }
            if (earthquake == null) {
                Timber.w("Unexpected view binding class {{ %s }}, list view item clicks will not work!", this.binding.getClass().getCanonicalName());
            } else {
                this.clickListener.onEarthquakeClick(v, earthquake);
            }
        }
    }

    public EarthquakeListViewModel(Earthquake earthquake) {
        super(earthquake);
    }
}
