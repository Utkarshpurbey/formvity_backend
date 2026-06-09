import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import { AppShell } from "./layouts/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { WorkspacesPage } from "./pages/WorkspacesPage";
import { WorkspaceFormsPage } from "./pages/WorkspaceFormsPage";
import { FormEditorPage } from "./pages/FormEditorPage";
import { FormResponsesPage } from "./pages/FormResponsesPage";
import { PublicFormPage } from "./pages/PublicFormPage";

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/f/:slug" element={<PublicFormPage />} />

      <Route
        path="/app"
        element={
          <PrivateRoute>
            <AppShell />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/app/workspaces" replace />} />
        <Route path="workspaces" element={<WorkspacesPage />} />
        <Route path="workspaces/:workspaceId" element={<WorkspaceFormsPage />} />
        <Route
          path="workspaces/:workspaceId/forms/:formId"
          element={<FormEditorPage />}
        />
        <Route
          path="workspaces/:workspaceId/forms/:formId/responses"
          element={<FormResponsesPage />}
        />
      </Route>

      <Route path="*" element={<Navigate to="/app/workspaces" replace />} />
    </Routes>
  );
}
