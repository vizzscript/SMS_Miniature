import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const Toast = ({message, onClose}) => {
  const [visible, setVisible] = useState (true);

  useEffect (
    () => {
      const timer = setTimeout (() => {
        setVisible (false);
        onClose ();
      },10000); // Auto-close after 3 seconds

      return () => clearTimeout (timer);
    },
    [onClose]
  );

  if (!visible) return null;

  return (
    <div className="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-4 animate-fade-in">
      <span>{message}</span>
      <button
        onClick={() => {
          setVisible (false);
          onClose ();
        }}
        className="text-white hover:text-gray-200 focus:outline-none"
      >
        &times;
      </button>
    </div>
  );
};
Toast.propTypes = {
  message: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default Toast;
