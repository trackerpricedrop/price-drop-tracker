import React from "react";

export const PromptModel = ({
  modelInfo,
  onLogin,
  onClose,
}: {
  modelInfo: {
    isSuccess: boolean;
    modelType: string;
    isOpen: boolean;
    message: string;
  };
  onLogin: () => void;
  onClose: () => void;
}) => {
  const { isOpen, modelType, isSuccess, message } = modelInfo;
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-xl shadow-lg max-w-sm w-full p-6 text-center">
        {modelType === "login" && (
          <>
            <h2 className="text-2xl font-bold mb-4 text-gray-800">
              Please log in to continue
            </h2>
            <p className="text-gray-600 mb-6">
              You need to be signed in to use this feature.
            </p>
            <div className="flex justify-center gap-4 mb-4">
              <button
                onClick={onLogin}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                Log In
              </button>
            </div>
          </>
        )}
        {modelType === "product-insert" && (
          <div className="mb-4">
            {isSuccess ? (
              <div>
                <h2 className="text-xl font-semibold text-green-700 mb-2">
                  {message}
                </h2>
              </div>
            ) : (
              <h2 className="text-xl font-semibold text-red-700 mb-2">
                {message}
              </h2>
            )}
          </div>
        )}
        <button
          onClick={onClose}
          className="border border-gray-300 text-gray-700 px-6 py-2 rounded-lg font-semibold hover:bg-gray-100 transition"
        >
          Cancel
        </button>
      </div>
    </div>
  );
};
